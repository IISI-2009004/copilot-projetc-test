package com.iisi.bookmanager.user.controller;

import com.iisi.bookmanager.user.dto.LoginRequest;
import com.iisi.bookmanager.user.dto.LoginResponse;
import com.iisi.bookmanager.user.dto.RegisterRequest;
import com.iisi.bookmanager.user.dto.UserResponse;
import com.iisi.bookmanager.user.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 註冊/登入端點（design.md 5c / tasks.md C5）。
 *
 * <p>端點：{@code POST /api/auth/register}、{@code POST /api/auth/login}。
 *
 * <p>TODO: 補上 {@code @Valid} 請求驗證與商業邏輯呼叫，屬於功能模組開發階段
 * （本階段僅建立端點簽章骨架）。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public UserResponse register(RegisterRequest request) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }

    @PostMapping("/login")
    public LoginResponse login(LoginRequest request) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }
}
