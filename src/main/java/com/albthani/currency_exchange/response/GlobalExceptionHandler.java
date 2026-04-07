package com.albthani.currency_exchange.response;

import com.albthani.currency_exchange.response.exception.BusinessException;
import com.albthani.currency_exchange.response.exception.ResourceNotFoundException;
import com.albthani.currency_exchange.response.exception.UnauthorizedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<GlobalResponse<?>> handleNoResource(NoResourceFoundException ex){
        List<GlobalResponse.ErrorItem> errors = List.of(new GlobalResponse.ErrorItem("Page Not Found"));
        return ResponseEntity.status(404).body(new GlobalResponse<>(errors));
    }

    // 404 - مورد غير موجود (مستخدم، طلب، ...)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GlobalResponse<?>> handleResourceNotFound(
            ResourceNotFoundException ex) {

        return ResponseEntity.status(404).body(
                new GlobalResponse<>(
                        List.of(new GlobalResponse.ErrorItem(ex.getMessage()))
                )
        );
    }

    // 400 - بيانات غلط من المستخدم (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse<?>> handleValidation(MethodArgumentNotValidException ex) {
        List<GlobalResponse.ErrorItem> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> new GlobalResponse.ErrorItem(err.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(400).body(new GlobalResponse<>(errors));
    }

    // 400 - أخطاء الـ Business Logic
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<GlobalResponse<?>> handleBusiness(
            BusinessException ex) {

        return ResponseEntity.status(400).body(
                new GlobalResponse<>(
                        List.of(new GlobalResponse.ErrorItem(ex.getMessage()))
                )
        );
    }

    // 401 - غير مصرح
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<GlobalResponse<?>> handleUnauthorized(
            UnauthorizedException ex) {

        return ResponseEntity.status(401).body(
                new GlobalResponse<>(
                        List.of(new GlobalResponse.ErrorItem(ex.getMessage()))
                )
        );
    }

    // أي خطأ غير متوقع
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse<?>> handleGeneral(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(500)
                .body(new GlobalResponse<>(List.of(new GlobalResponse.ErrorItem("حدث خطأ غير متوقع"))));
    }
}
