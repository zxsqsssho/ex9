//src/main/java/com/library/controller/BooksController.java
package com.library.controller;

import com.library.entity.Books;
import com.library.service.BooksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BooksController {

    @Autowired
    private BooksService booksService;

    @GetMapping("/books")
    public String getBooks(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size) {
        // 构建JPA分页请求（页码从0开始）
        PageRequest pageRequest = PageRequest.of(page, size);
        // 调用Service获取分页数据
        Page<Books> booksPage = booksService.findAll(pageRequest);
        // 传递到前端页面
        model.addAttribute("booksPage", booksPage);
        return "books/list";
    }
}