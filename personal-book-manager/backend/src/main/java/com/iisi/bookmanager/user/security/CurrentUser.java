package com.iisi.bookmanager.user.security;

/**
 * 供 book/reading 模組的 Controller/Service 取得目前登入者 {@code userId} 的工具類別
 * （design.md 5c）。
 *
 * <p>內部委派 {@code SecurityContextHolder.getContext().getAuthentication().getPrincipal()}，
 * 是 book/reading 模組唯一允許依賴的 user 模組類別，維持「book/reading 不得直接依賴
 * user 模組其他任何類別」的邊界規則。
 *
 * <p>TODO: 實作內部委派邏輯，屬於功能模組開發階段（本階段僅建立骨架）。
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long id() {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }
}
