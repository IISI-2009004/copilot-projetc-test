package com.iisi.bookmanager.user.service;

import com.iisi.bookmanager.user.dto.LoginRequest;
import com.iisi.bookmanager.user.dto.LoginResponse;
import com.iisi.bookmanager.user.dto.RegisterRequest;
import com.iisi.bookmanager.user.dto.UserResponse;

/**
 * 使用者註冊/登入核心邏輯（design.md 5c / tasks.md C2）。
 */
public interface UserService {

    /**
     * 註冊新帳號。
     *
     * @param request 註冊請求（username/password 已通過 Bean Validation）
     * @return 註冊成功的使用者資訊（不含密碼）
     */
    UserResponse register(RegisterRequest request);

    /**
     * 登入並簽發 JWT。
     *
     * @param request 登入請求
     * @return 登入回應（含 JWT）
     */
    LoginResponse login(LoginRequest request);

    /**
     * 取得目前登入者資訊。
     *
     * @param userId 由 {@code CurrentUser.id()} 取得的使用者 ID
     * @return 使用者資訊（不含密碼）
     */
    UserResponse getCurrentUser(Long userId);
}
