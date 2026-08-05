package com.iisi.bookmanager.user.service;

import com.iisi.bookmanager.user.domain.User;
import com.iisi.bookmanager.user.dto.LoginRequest;
import com.iisi.bookmanager.user.dto.LoginResponse;
import com.iisi.bookmanager.user.dto.RegisterRequest;
import com.iisi.bookmanager.user.dto.UserResponse;
import com.iisi.bookmanager.user.exception.DuplicateUsernameException;
import com.iisi.bookmanager.user.exception.InvalidCredentialsException;
import com.iisi.bookmanager.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * {@link UserService} 實作（design.md 5c / tasks.md C2）。
 *
 * <p>密碼一律以 {@link PasswordEncoder}（bcrypt，cost ≥ 10，由 {@code SecurityConfig} 提供 Bean）雜湊後儲存，
 * 明文密碼絕不寫入資料庫或日誌。
 */
@Service
public class UserServiceImpl implements UserService {

    /** 帳號不存在或密碼錯誤一律回傳同一則訊息，避免帳號列舉（Account Enumeration）攻擊。 */
    private static final String INVALID_CREDENTIALS_MESSAGE = "帳號或密碼錯誤";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 建構子注入。
     *
     * @param userRepository   User 資料存取
     * @param passwordEncoder  密碼雜湊器（由 SecurityConfig 提供 Bean，便於測試 mock）
     * @param jwtTokenProvider JWT 簽發/驗證
     */
    public UserServiceImpl(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new DuplicateUsernameException("帳號已被使用");
        }
        String passwordHash = passwordEncoder.encode(request.password());
        User saved = userRepository.save(new User(request.username(), passwordHash));
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Optional<User> user = userRepository.findByUsernameIgnoreCase(request.username());
        if (user.isEmpty() || !passwordEncoder.matches(request.password(), user.get().getPasswordHash())) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }
        User found = user.get();
        String token = jwtTokenProvider.generateToken(found.getId(), found.getUsername());
        return new LoginResponse(token, "Bearer", jwtTokenProvider.getExpirationSeconds());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE));
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getCreatedAt());
    }
}

