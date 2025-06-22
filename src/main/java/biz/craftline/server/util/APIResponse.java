package biz.craftline.server.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIResponse<T> {

    private boolean success;
    private String message;
    private int statusCode;
    private LocalDateTime timestamp;
    private T data;
    private List<String> errors;
    private Pagination pagination;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    // --- SUCCESS FACTORIES ---

    public static <T> ResponseEntity<APIResponse<T>> success(T data) {
        return success(data, "Success", HttpStatus.OK);
    }

    public static <T> ResponseEntity<APIResponse<T>> success(T data, String message) {
        return success(data, message, HttpStatus.OK);
    }

    public static <T> ResponseEntity<APIResponse<T>> success(T data, String message, HttpStatus status) {
        APIResponse<T> body = new APIResponse<>();
        body.setSuccess(true);
        body.setMessage(message);
        body.setStatusCode(status.value());
        body.setTimestamp(LocalDateTime.now());
        body.setData(data);
        return new ResponseEntity<>(body, status);
    }

    public static <T> ResponseEntity<APIResponse<T>> success(T data, String message, HttpStatus status, Pagination pagination) {
        APIResponse<T> body = new APIResponse<>();
        body.setSuccess(true);
        body.setMessage(message);
        body.setStatusCode(status.value());
        body.setTimestamp(LocalDateTime.now());
        body.setData(data);
        body.setPagination(pagination);
        return new ResponseEntity<>(body, status);
    }

    // --- ERROR FACTORIES ---

    public static <T> ResponseEntity<APIResponse<T>> error(String message, HttpStatus status) {
        return error(message, status, null);
    }

    public static <T> ResponseEntity<APIResponse<T>> error(String message, HttpStatus status, List<String> errors) {
        APIResponse<T> body = new APIResponse<>();
        body.setSuccess(false);
        body.setMessage(message);
        body.setStatusCode(status.value());
        //body.setTimestamp(LocalDateTime.now());
        body.setErrors(errors);
        return new ResponseEntity<>(body, status);
    }

    // --- Pagination inner class ---

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pagination {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }
}
