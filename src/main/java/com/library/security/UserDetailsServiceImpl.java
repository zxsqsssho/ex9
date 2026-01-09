//src/main/java/com/library/security/UserDetailsServiceImpl.java
package com.library.security;

import com.library.entity.User;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        //orElseThrow是Optional类的一个方法，它的作用是：如果Optional中有值，则返回该值；
        // 如果Optional中没有值（即为空），则抛出一个指定的异常。

        // 检查用户状态
        if (user.getStatus() == User.UserStatus.INACTIVE) {
            throw new UsernameNotFoundException("账户已停用");
        }
        if (user.getStatus() == User.UserStatus.LOCKED) {
            throw new UsernameNotFoundException("账户已锁定");
        }
        if (user.getStatus() == User.UserStatus.DELETED) {
            throw new UsernameNotFoundException("账户不存在");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .accountExpired(false)//设置账户是否过期。如果为true，则账户已过期，无法使用。
                .accountLocked(false)//设置账户是否被锁定。如果为true，则账户被锁定，无法使用。
                .credentialsExpired(false)//设置凭证（密码）是否过期。如果为true，则密码已过期，需要修改密码。
                .disabled(false)//设置账户是否被禁用。如果为true，则账户被禁用，无法使用。
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        // 获取角色和权限
        Collection<SimpleGrantedAuthority> authorities = user.getRole().getPermissions().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        // 作用：把用户的角色所拥有的具体权限转换成Spring Security认识的格式
        // 例如：["user:read", "book:borrow"] → [SimpleGrantedAuthority("user:read"), SimpleGrantedAuthority("book:borrow")]

        // 添加角色权限
        authorities.add(new SimpleGrantedAuthority(user.getRole().getName()));

        // 根据用户类型添加默认角色
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getUserType().name()));

        return authorities;
    }
}