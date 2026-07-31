package com.iisi.bookmanager.user.controller;

import com.iisi.bookmanager.user.dto.UserResponse;
import com.iisi.bookmanager.user.security.CurrentUser;
import com.iisi.bookmanager.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 目前登入使用者資訊端點（design.md 5c / tasks.md C5）。
 *
 * <p>端點：{@code GET /api/users/me}（需登入，見 {@code SecurityConfig}）。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * 建構子注入。
     *
     * @param userService 使用者商業邏輯
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 取得目前登入者資訊。
     *
     * @return 200 使用者資訊（不含密碼）
     */
    @GetMapping("/me")
    public UserResponse me() {
        Long userId = CurrentUser.id();
        return userService.getCurrentUser(userId);
    }
}
