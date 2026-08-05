package com.iisi.bookmanager.user.repository;

import com.iisi.bookmanager.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * User 資料存取介面（design.md 3. 資料模型 / tasks.md C1）。
 *
 * <p>所有查詢皆使用 Spring Data JPA 衍生查詢方法（參數化查詢，禁止字串拼接）。
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 檢查帳號是否已存在（不分大小寫比對唯一性）。
     *
     * @param username 帳號
     * @return 是否已存在
     */
    boolean existsByUsernameIgnoreCase(String username);

    /**
     * 依帳號查詢使用者（不分大小寫）。
     *
     * @param username 帳號
     * @return 使用者（可能為空）
     */
    Optional<User> findByUsernameIgnoreCase(String username);
}
