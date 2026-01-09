//src/main/java/com/library/mapper/BooksMapper.java
package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Books;
import com.library.dto.BookQueryDTO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface BooksMapper extends BaseMapper<Books> {
    /**
     * 多条件查询图书
     * @param queryDTO 查询条件
     * @return 图书列表
     */
    List<Books> queryBooksByCondition(@Param("dto") BookQueryDTO queryDTO);
}
