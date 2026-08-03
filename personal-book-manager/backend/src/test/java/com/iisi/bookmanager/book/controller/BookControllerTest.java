package com.iisi.bookmanager.book.controller;

import com.iisi.bookmanager.book.domain.BookType;
import com.iisi.bookmanager.book.dto.BookResponse;
import com.iisi.bookmanager.book.exception.DuplicateIsbnException;
import com.iisi.bookmanager.book.service.BookService;
import com.iisi.bookmanager.user.security.SecurityConfig;
import com.iisi.bookmanager.user.service.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code POST /api/books} 整合測試（tasks.md A3/A5）。
 *
 * <p>使用 {@code @WebMvcTest} 載入實際 {@link SecurityConfig}，驗證：
 * 需登入（未帶認證標頭回 401）、Bean Validation 失敗回 400、
 * 去重衝突（{@link DuplicateIsbnException}）回 409、成功建立回 201。
 *
 * <p>本測試檔實體位置放在 {@code book} 套件目錄下（本環境檔案建立工具不支援自動建立
 * 巢狀子目錄），package 宣告仍對應正確的來源套件 {@code com.iisi.bookmanager.book.controller}。
 */
@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerTest {

    private static final String AUTH_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String AUTH_SCHEME = "Bearer";
    private static final String VALID_TOKEN = "valid-token-string";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private void mockAuthenticatedAs(Long userId) {
        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUserId(VALID_TOKEN)).thenReturn(userId);
    }

    @Test
    @DisplayName("當未帶認證標頭呼叫 POST /api/books 時，應回傳 401")
    void should_Return401_When_NoAuthorizationHeaderProvided() throws Exception {
        // Arrange
        String body = objectMapper.writeValueAsString(
                new BookRequestFixture("9780000000001", null, null, "title", "author", "PHYSICAL_BOOK", null, null, null));

        // Act & Assert
        mockMvc.perform(post("/api/books")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("當 title 為空白時，POST /api/books 應回傳 400")
    void should_Return400_When_TitleBlank() throws Exception {
        // Arrange
        mockAuthenticatedAs(1L);
        String body = objectMapper.writeValueAsString(
                new BookRequestFixture("9780000000001", null, null, "", "author", "PHYSICAL_BOOK", null, null, null));

        // Act & Assert
        mockMvc.perform(post("/api/books")
                        .header(AUTH_HEADER, AUTH_SCHEME + " " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("當實體書 bookType 卻填了 url 時，POST /api/books 應回傳 400（違反 ValidBookType 互斥規則）")
    void should_Return400_When_PhysicalBookTypeHasUrl() throws Exception {
        // Arrange
        mockAuthenticatedAs(1L);
        String body = objectMapper.writeValueAsString(
                new BookRequestFixture(null, "https://example.com", null, "title", "author",
                        "PHYSICAL_BOOK", null, null, null));

        // Act & Assert
        mockMvc.perform(post("/api/books")
                        .header(AUTH_HEADER, AUTH_SCHEME + " " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("當 Service 拋出 DuplicateIsbnException 時，POST /api/books 應回傳 409")
    void should_Return409_When_DuplicateIsbn() throws Exception {
        // Arrange
        mockAuthenticatedAs(1L);
        when(bookService.createBook(anyLong(), any())).thenThrow(new DuplicateIsbnException("重複 ISBN"));
        String body = objectMapper.writeValueAsString(
                new BookRequestFixture("9780000000001", null, null, "title", "author",
                        "PHYSICAL_BOOK", null, null, null));

        // Act & Assert
        mockMvc.perform(post("/api/books")
                        .header(AUTH_HEADER, AUTH_SCHEME + " " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("當請求合法且無衝突時，POST /api/books 應回傳 201 與建立後的 BookResponse")
    void should_Return201_When_RequestValid() throws Exception {
        // Arrange
        mockAuthenticatedAs(1L);
        BookResponse response = new BookResponse(100L, "9780000000001", null, null, "title", "author",
                BookType.PHYSICAL_BOOK, null, null, null, null, null,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"));
        when(bookService.createBook(anyLong(), any())).thenReturn(response);
        String body = objectMapper.writeValueAsString(
                new BookRequestFixture("9780000000001", null, null, "title", "author",
                        "PHYSICAL_BOOK", null, null, null));

        // Act & Assert
        mockMvc.perform(post("/api/books")
                        .header(AUTH_HEADER, AUTH_SCHEME + " " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.title").value("title"));
    }

    /**
     * 測試用 JSON fixture（避免直接依賴 record 建構順序，維持測試可讀性）。
     */
    private record BookRequestFixture(String isbn, String url, String sourcePlatform, String title,
                                       String author, String bookType, Long categoryId,
                                       String purchaseUrl, String authorUrl) {
    }
}
