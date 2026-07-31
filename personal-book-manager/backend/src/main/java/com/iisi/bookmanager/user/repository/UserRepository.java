package com.iisi.bookmanager.user.repository;

import com.iisi.bookmanager.user.domain.User;

import java.util.Optional;

/**
 * User 資料存取介面（design.md 3. 資料模型 / tasks.md C1）。
 *
 * <p>TODO: 改為繼承 {@code JpaRepository<User, Long>}，屬於功能模組開發階段。
 */
public interface UserRepository {

    boolean existsByUsernameIgnoreCase(String username);

    Optional<User> findByUsernameIgnoreCase(String username);
}
