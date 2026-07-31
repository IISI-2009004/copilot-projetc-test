package com.iisi.bookmanager.book.domain;

/**
 * 藏書類型分類（design.md 5a / ADR-0004）。
 */
public enum BookType {
    PHYSICAL_BOOK,       // 實體書籍（一般出版品，通常有 ISBN）
    PHYSICAL_DOUJINSHI,  // 實體同人誌（紙本二創，通常無 ISBN）
    EBOOK,               // 電子書（可能有 ISBN）
    WEB_NOVEL,           // 網路小說（連載平台，如巴哈姆特、Wattpad）
    BLOG_POST,           // Blog 文章
    ONLINE_FANFIC;       // 同人文網站作品（如 AO3）

    /** 是否為「實體／電子書」類（可能有 ISBN，不可有 URL）。 */
    public boolean isPhysicalOrEbook() {
        return this == PHYSICAL_BOOK || this == PHYSICAL_DOUJINSHI || this == EBOOK;
    }

    /** 是否為「線上內容」類（必須有 URL，不可有 ISBN）。 */
    public boolean isOnline() {
        return !isPhysicalOrEbook();
    }

    /** 是否需要於新增/更新時執行 ISBN 去重（僅實體類，EBOOK 允許多平台重複購買不去重）。 */
    public boolean requiresIsbnDedup() {
        return this == PHYSICAL_BOOK || this == PHYSICAL_DOUJINSHI;
    }
}
