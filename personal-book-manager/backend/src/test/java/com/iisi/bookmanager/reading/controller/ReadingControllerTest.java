package com.iisi.bookmanager.reading.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iisi.bookmanager.reading.domain.ReadingSource;
import com.iisi.bookmanager.reading.dto.ReadingResponse;
import com.iisi.bookmanager.reading.dto.StatsResponse;
import com.iisi.bookmanager.reading.exception.ReadingRecordNotFoundException;
import com.iisi.bookmanager.reading.exception.ReferencedBookNotFoundException;
import com.iisi.bookmanager.reading.service.ReadingCalendarService;
import com.iisi.bookmanager.reading.service.ReadingService;
import com.iisi.bookmanager.user.security.SecurityConfig;
import com.iisi.bookmanager.user.service.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code /api/reading} 端點整合測試（tasks.md B4/B5）。
 *
 * <p>使用 {@code @WebMvcTest} 載入實際 {@link SecurityConfig}，驗證：需登入
 * （未帶認證標頭回 401）、Bean Validation 失敗回 400（含 {@code @ValidReadingSource}
 * 互斥規則）、書本不存在回 404、成功新增回 201。
 *
 * <p>本測試檔實體位置放在 {@code reading} 套件目錄下（本環境檔案建立工具不支援自動建立
 * 巢狀子目錄），package 宣告仍對應正確的來源套件 {@code com.iisi.bookmanager.reading.controller}。
 */
@WebMvcTest(ReadingController.class)
@Import(SecurityConfig.class)
class ReadingControllerTest {

    private static final String AUTH_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String AUTH_SCHEME = "Bearer";
    private static final String VALID_TOKEN = "valid-token-string";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReadingService readingService;

    @MockBean
    private ReadingCalendarService readingCalendarService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private void mockAuthenticatedAs(Long userId) {
        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUserId(VALID_TOKEN)).thenReturn(userId);
    }

    @Test
    @DisplayName("當未帶認證標頭呼叫 POST /api/reading 時，應回傳 401")
    void should_Return401_When_NoAuthorizationHeaderProvided() throws Exception {
        // Arrange
        String body = objectMapper.writeValueAsString(
                new ReadingRequestFixture("OWNED", 10L, null, null, "2026-08-01", 30, 50));

        // Act & Assert
        mockMvc.perform(post("/api/reading")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("當 source=OWNED 卻缺少 bookId 時，POST /api/reading 應回傳 400（違反 ValidReadingSource 互斥規則）")
    void should_Return400_When_OwnedSourceMissingBookId() throws Exception {
        // Arrange
        mockAuthenticatedAs(1L);
        String body = objectMapper.writeValueAsString(
                new ReadingRequestFixture("OWNED", null, null, null, "2026-08-01", 30, 50));

        // Act & Assert
        mockMvc.perform(post("/api/reading")
                        .header(AUTH_HEADER, AUTH_SCHEME + " " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("當 source≠OWNED 卻缺少 externalTitle 時，POST /api/reading 應回傳 400")
    void should_Return400_When_BorrowedSourceMissingExternalTitle() throws Exception {
        // Arrange
        mockAuthenticatedAs(1L);
        String body = objectMapper.writeValueAsString(
                new ReadingRequestFixture("BORROWED_FRIEND", null, null, null, "2026-08-01", 30, 50));

        // Act & Assert
        mockMvc.perform(post("/api/reading")
                        .header(AUTH_HEADER, AUTH_SCHEME + " " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("當 source≠OWNED 卻誤帶 bookId 時，POST /api/reading 應回傳 400")
    void should_Return400_When_BorrowedSourceHasBookId() throws Exception {
        // Arrange
        mockAuthenticatedAs(1L);
        String body = objectMapper.writeValueAsString(
                new ReadingRequestFixture("BORROWED_FRIEND", 10L, "借來的書", "作者", "2026-08-01", 30, 50));

        // Act & Assert
        mockMvc.perform(post("/api/reading")
                        .header(AUTH_HEADER, AUTH_SCHEME + " " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("當 durationMinutes 為 0（低於邊界 1）時，POST /api/reading 應回傳 400")
    void should_Return400_When_DurationMinutesIsZero() throws Exception {
        // Arrange
        mockAuthenticatedAs(1L);
        String body = objectMapper.writeValueAsString(
                new ReadingRequestFixture("OWNED", 10L, null, null, "2026-08-01", 0, 50));

        // Act & Assert
        mockMvc.perform(post("/api/reading")
                        .header(AUTH_HEADER, AUTH_SCHEME + " " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("當 Service 拋出 ReferencedBookNotFoundException 時，POST /api/reading 應回傳 404")
    void should_Return404_When_ReferencedBookNotFound() throws Exception {
        // Arrange
        mockAuthenticatedAs(1L);
        when(readingService.addRecord(anyLong(), any()))
                .thenThrow(new ReferencedBookNotFoundException("書本不存在"));
        String body = objectMapper.writeValueAsString(
                new ReadingRequestFixture("OWNED", 999L, null, null, "2026-08-01", 30, 50));

        // Act & Assert
        mockMvc.perform(post("/api/reading")
                        .header(AUTH_HEADER, AUTH_SCHEME + " " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("當請求合法且書本存在時，POST /api/reading 應回傳 201 與建立後的 ReadingResponse")
    void should_Return201_When_RequestValid() throws Exception {
        // Arrange
        mockAuthenticatedAs(1L);
        ReadingResponse response = new ReadingResponse(100L, ReadingSource.OWNED, 10L, null, null,
                LocalDate.of(2026, 8, 1), 30, 50, Instant.parse("2026-08-01T00:00:00Z"));
        when(readingService.addRecord(anyLong(), any())).thenReturn(response);
        String body = objectMapper.writeValueAsString(
                new ReadingRequestFixture("OWNED", 10L, null, null, "2026-08-01", 30, 50));

        // Act & Assert
        mockMvc.perform(post("/api/reading")
                        .header(AUTH_HEADER, AUTH_SCHEME + " " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.durationMinutes").value(30));
    }

    @Test
    @DisplayName("當更新的記錄不存在時，PUT /api/reading/{id} 應回傳 404")
    void should_Return404_When_UpdatingNonExistentRecord() throws Exception {
        // Arrange
        mockAuthenticatedAs(1L);
        when(readingService.updateRecord(anyLong(), eq(999L), any()))
                .thenThrow(new ReadingRecordNotFoundException("找不到記錄"));
        String body = objectMapper.writeValueAsString(
                new ReadingRequestFixture("OWNED", 10L, null, null, "2026-08-01", 30, 50));

        // Act & Assert
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/reading/999")
                        .header(AUTH_HEADER, AUTH_SCHEME + " " + VALID_TOKEN)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("當 GET /api/reading 未帶 bookId 或 source 時，應回傳 400")
    void should_Return400_When_ListRecordsMissingBookIdAndSource() throws Exception {
        // Arrange
        mockAuthenticatedAs(1L);

        // Act & Assert
        mockMvc.perform(get("/api/reading")
                        .header(AUTH_HEADER, AUTH_SCHEME + " " + VALID_TOKEN))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("當 GET /api/reading/stats 請求合法時，應回傳統計資料")
    void should_ReturnStats_When_RequestingStats() throws Exception {
        // Arrange
        mockAuthenticatedAs(1L);
        when(readingService.getStats(1L)).thenReturn(new StatsResponse(120L, 3L));

        // Act & Assert
        mockMvc.perform(get("/api/reading/stats")
                        .header(AUTH_HEADER, AUTH_SCHEME + " " + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMinutes").value(120))
                .andExpect(jsonPath("$.completedBooksCount").value(3));
    }

    /**
     * 測試用 JSON fixture（避免直接依賴 record 建構順序，維持測試可讀性）。
     */
    private record ReadingRequestFixture(String source, Long bookId, String externalTitle, String externalAuthor,
                                          String readDate, int durationMinutes, int progressPercent) {
    }
}
