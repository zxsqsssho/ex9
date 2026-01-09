//src/main/java/com/library/controller/BooksController.java
package com.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.Books;
import com.library.dto.BookQueryDTO;
import com.library.service.BooksService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@CrossOrigin // 跨域（生产环境需配置白名单）
public class BooksController {

    @Resource
    private BooksService booksService;

    /**
     * 多条件分页查询图书
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @PostMapping("/query")
    public Page<Books> queryBooks(@RequestBody BookQueryDTO queryDTO) {
        return booksService.queryBooksByCondition(queryDTO);
    }

    /**
     * 全局查询（跨分馆）
     * @param queryDTO 查询条件
     * @return 图书列表
     */
    @PostMapping("/global-query")
    public List<Books> globalQuery(@RequestBody BookQueryDTO queryDTO) {
        return booksService.globalQueryBooks(queryDTO);
    }

    /**
     * 查询图书详情
     * @param bookId 图书ID
     * @return 图书详情
     */
    @GetMapping("/{bookId}")
    public Books getBookDetail(@PathVariable Integer bookId) {
        return booksService.getBookDetail(bookId);
    }
}
