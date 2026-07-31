package com.iisi.bookmanager.user.service;

import com.iisi.bookmanager.user.dto.LoginRequest;
import com.iisi.bookmanager.user.dto.LoginResponse;
import com.iisi.bookmanager.user.dto.RegisterRequest;
import com.iisi.bookmanager.user.dto.UserResponse;
import org.springframework.stereotype.Service;

/**
 * {@link UserService} 骨架實作（本階段僅供 Spring context 正常啟動，不含商業邏輯）。
 *
 * <p>TODO: 注入 {@code UserRepository}/{@code JwtTokenProvider} 並實作 tasks.md C2
 * 的商業邏輯，屬於功能模組開發階段。
 */
@Service
public class UserServiceImpl implements UserService {

    @Override
    public UserResponse register(RegisterRequest request) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }

    @Override
    public UserResponse getCurrentUser(Long userId) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }
}
