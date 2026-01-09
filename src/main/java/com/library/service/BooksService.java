//src/main/java/com/library/service/BooksService.java
package com.library.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.Books;
import com.library.dto.BookQueryDTO;
import java.util.List;

public interface BooksService {
    /**
     * 多条件分页查询图书
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<Books> queryBooksByCondition(BookQueryDTO queryDTO);

    /**
     * 全局查询（跨分馆）
     * @param queryDTO 查询条件
     * @return 图书列表
     */
    List<Books> globalQueryBooks(BookQueryDTO queryDTO);

    /**
     * 根据ID查询图书详情
     * @param bookId 图书ID
     * @return 图书详情
     */
    Books getBookDetail(Integer bookId);
}