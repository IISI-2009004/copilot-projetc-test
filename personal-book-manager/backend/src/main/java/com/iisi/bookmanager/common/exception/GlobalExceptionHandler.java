package com.iisi.bookmanager.common.exception;

import com.iisi.bookmanager.book.exception.BookNotFoundException;
import com.iisi.bookmanager.book.exception.CategoryInUseException;
import com.iisi.bookmanager.book.exception.CategoryNotFoundException;
import com.iisi.bookmanager.book.exception.DuplicateIsbnException;
import com.iisi.bookmanager.book.exception.DuplicateUrlException;
import com.iisi.bookmanager.book.exception.ImageTooLargeException;
import com.iisi.bookmanager.book.exception.InvalidImageFileException;
import com.iisi.bookmanager.reading.exception.ReadingRecordNotFoundException;
import com.iisi.bookmanager.user.exception.DuplicateUsernameException;
import com.iisi.bookmanager.user.exception.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

/**
 * 全域例外處理器（design.md copilot-instructions.md / 各模組 exception 對照）。
 *
 * <p>統一將各模組自訂例外轉換為 {@link ErrorResponse}，不輸出堆疊細節；
 * 各狀態碼對照詳見 docs/design/API_Spec_Template.md 與各模組 tasks.md。
 *
 * <p>TODO: 補上完整的 {@code @ExceptionHandler} 對照（含 Bean Validation
 * MethodArgumentNotValidException 等），屬於功能模組開發階段（本階段僅建立骨架）。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFound(BookNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex, request);
    }

    @ExceptionHandler(ReadingRecordNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReadingRecordNotFound(ReadingRecordNotFoundException ex,
                                                                       WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex, request);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFound(CategoryNotFoundException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex, request);
    }

    @ExceptionHandler({DuplicateIsbnException.class, DuplicateUrlException.class,
            CategoryInUseException.class, DuplicateUsernameException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex, request);
    }

    @ExceptionHandler({InvalidImageFileException.class, ImageTooLargeException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex, request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex,
                                                                    WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex, request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, RuntimeException ex, WebRequest request) {
        ErrorResponse body = new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(),
                ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(status).body(body);
    }
}
