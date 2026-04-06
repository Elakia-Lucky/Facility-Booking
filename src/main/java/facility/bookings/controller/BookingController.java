package facility.bookings.controller;

import facility.bookings.dto.BookingResponse;
import facility.bookings.dto.CreateBookingRequest;
import facility.bookings.service.BookingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest req) {

        BookingResponse resp = bookingService.createBooking(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }


    @GetMapping
    public ResponseEntity<List<BookingResponse>> listBookings(
            @RequestParam @NotBlank(message = "userId is required") String userId) {
        return ResponseEntity.ok(bookingService.getBookingsForUser(userId));
    }
}
