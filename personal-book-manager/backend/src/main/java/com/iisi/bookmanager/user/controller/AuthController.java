package com.iisi.bookmanager.user.controller;

import com.iisi.bookmanager.user.dto.LoginRequest;
import com.iisi.bookmanager.user.dto.LoginResponse;
import com.iisi.bookmanager.user.dto.RegisterRequest;
import com.iisi.bookmanager.user.dto.UserResponse;
import com.iisi.bookmanager.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 註冊/登入端點（design.md 5c / tasks.md C5）。
 *
 * <p>端點：{@code POST /api/auth/register}、{@code POST /api/auth/login}；
 * 兩者皆為 {@code permitAll}（見 {@code SecurityConfig}），不需攜帶 JWT。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    /**
     * 建構子注入。
     *
     * @param userService 使用者商業邏輯
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 註冊新帳號。
     *
     * @param request 註冊請求（經 Bean Validation 驗證）
     * @return 201 建立成功的使用者資訊（不含密碼）
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 登入並簽發 JWT。
     *
     * @param request 登入請求
     * @return 200 登入回應（含 JWT）
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }
}
