package com.iisi.bookmanager.book.domain;

/**
 * 書本標籤 Entity（design.md 2. 套件結構）。
 *
 * <p>TODO: 補上 JPA 註解與 userId 隔離欄位，屬於功能模組開發階段。
 */
public class Tag {

    private Long id;
    private Long userId;
    private String name;

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }
}
