//src/main/java/com/library/service/impl/BooksServiceImpl.java
package com.library.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.Books;
import com.library.dto.BookQueryDTO;
import com.library.mapper.BooksMapper;
import com.library.service.BooksService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class BooksServiceImpl extends ServiceImpl<BooksMapper, Books> implements BooksService {

    @Resource
    private BooksMapper booksMapper;

    @Override
    public Page<Books> queryBooksByCondition(BookQueryDTO queryDTO) {
        // MyBatis-Plus分页
        Page<Books> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        return page.setRecords(booksMapper.queryBooksByCondition(queryDTO));
    }

    @Override
    public List<Books> globalQueryBooks(BookQueryDTO queryDTO) {
        // 全局查询（不分页，可根据需求调整）
        return booksMapper.queryBooksByCondition(queryDTO);
    }

    @Override
    public Books getBookDetail(Integer bookId) {
        return baseMapper.selectById(bookId);
    }
}
