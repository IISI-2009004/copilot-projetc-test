package com.iisi.bookmanager.book.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * {@link CoverStorageService} 的 MVP 實作：寫入本機磁碟固定目錄（design.md 5b）。
 *
 * <p>檔名以 UUID 產生（不使用使用者提供的檔名，避免路徑穿越／檔名衝突），
 * 透過 Spring 靜態資源設定對外提供 {@code GET /covers/{uuid}.{ext}}。
 *
 * <p>TODO: 實作檔案讀寫、magic bytes 驗證、5MB 大小限制邏輯，屬於功能模組開發階段
 * （本階段僅建立骨架）。
 */
@Service
public class LocalDiskCoverStorageService implements CoverStorageService {

    @Override
    public String store(Long bookId, MultipartFile file) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }

    @Override
    public void delete(String storedPath) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }
}
