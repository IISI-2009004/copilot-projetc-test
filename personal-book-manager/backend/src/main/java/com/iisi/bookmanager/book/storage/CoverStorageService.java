package com.iisi.bookmanager.book.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 封面圖片儲存介面（design.md 5b / ADR-0005）。
 *
 * <p>MVP 實作為 {@code LocalDiskCoverStorageService}；未來可平行實作
 * {@code S3CoverStorageService} 並替換 Bean 而不影響 BookService 邏輯。
 */
public interface CoverStorageService {

    /** 儲存檔案並回傳可對外存取的 URL（相對路徑或靜態資源路徑）。 */
    String store(Long bookId, MultipartFile file);

    /** 刪除既有的已上傳檔案（by 儲存路徑），找不到則靜默略過。 */
    void delete(String storedPath);
}
