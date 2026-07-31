package com.iisi.bookmanager.user.controller;

import com.iisi.bookmanager.user.dto.UserResponse;
import com.iisi.bookmanager.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 目前登入使用者資訊端點（design.md 5c / tasks.md C5）。
 *
 * <p>端點：{@code GET /api/users/me}。
 *
 * <p>TODO: 透過 {@code CurrentUser.id()} 取得 userId 並呼叫 UserService，
 * 屬於功能模組開發階段（本階段僅建立端點簽章骨架）。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me() {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }
}
