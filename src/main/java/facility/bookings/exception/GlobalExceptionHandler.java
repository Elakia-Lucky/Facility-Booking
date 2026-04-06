package facility.bookings.exception;

import facility.bookings.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> msgs = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();

        return ResponseEntity.badRequest().body(buildError(HttpStatus.BAD_REQUEST, msgs));
    }

    @ExceptionHandler(BookingValidationException.class)
    public ResponseEntity<ErrorResponse> handleBookingValidation(BookingValidationException ex) {
        return ResponseEntity.badRequest().body(buildError(HttpStatus.BAD_REQUEST, List.of(ex.getMessage())));
    }

    @ExceptionHandler(BookingConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(BookingConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, List.of(ex.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR, List.of("An unexpected error occurred.")));
    }

    private ErrorResponse buildError(HttpStatus status, List<String> msgs) {
        return ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .msgs(msgs)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
