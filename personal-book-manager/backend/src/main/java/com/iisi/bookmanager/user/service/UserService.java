package com.iisi.bookmanager.user.service;

import com.iisi.bookmanager.user.dto.LoginRequest;
import com.iisi.bookmanager.user.dto.LoginResponse;
import com.iisi.bookmanager.user.dto.RegisterRequest;
import com.iisi.bookmanager.user.dto.UserResponse;

/**
 * 使用者註冊/登入核心邏輯（design.md 5c / tasks.md C2）。
 *
 * <p>TODO: 實作 register/login/getCurrentUser 商業邏輯，屬於功能模組開發階段
 * （本階段僅建立方法簽章與骨架）。
 */
public interface UserService {

    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserResponse getCurrentUser(Long userId);
}
