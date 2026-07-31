package com.iisi.bookmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 個人圖書管理系統（Personal Book Manager）應用程式進入點。
 *
 * <p>模組概觀（詳見 {@code docs/design/design.md} 第 1、2 節）：
 * <ul>
 *   <li>{@code user} — 使用者註冊/登入、JWT 簽發與驗證（最底層模組，不依賴 book/reading）</li>
 *   <li>{@code book} — 書本、標籤、分類管理，對外僅暴露 {@code BookQueryPort} 介面</li>
 *   <li>{@code reading} — 閱讀記錄與日曆，透過 {@code BookQueryPort} 單向查詢 book 模組</li>
 *   <li>{@code common} — 全域例外處理、共用設定等橫切關注點</li>
 * </ul>
 */
@SpringBootApplication
public class PersonalBookManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonalBookManagerApplication.class, args);
    }
}
