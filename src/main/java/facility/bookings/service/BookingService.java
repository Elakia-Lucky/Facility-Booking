package facility.bookings.service;

import facility.bookings.dto.BookingResponse;
import facility.bookings.dto.CreateBookingRequest;
import facility.bookings.exception.BookingConflictException;
import facility.bookings.exception.BookingValidationException;
import facility.bookings.model.Booking;
import facility.bookings.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final int MAX_HOURS = 2;
	private static final String EXEC_MAX_DURATION = "Booking duration cannot exceed 2 hours.";
	private static final String EXEC_BOOKING_TIME = "A booking must have a start time and an end time.";
	private static final String EXEC_PAST_BOOKING_START_TIME = "Booking start time cannot be in the past.";
	private static final String EXEC_END_TIME="End time must be after start time.";

    private final BookingRepository bookingRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingResponse createBooking(CreateBookingRequest request) {
        validate(request);

        List<Booking> conflicts = bookingRepository.findOverlapping(
                request.getFacilityId(),
                request.getStartTime(),
                request.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            throw new BookingConflictException(request.getFacilityId());
        }

        Booking saved = bookingRepository.save(
                Booking.builder()
                        .facilityId(request.getFacilityId())
                        .userId(request.getUserId())
                        .startTime(request.getStartTime())
                        .endTime(request.getEndTime())
                        .build()
        );

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsForUser(String userId) {
        return bookingRepository.findByUserIdOrderByStartTimeAsc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    private void validate(CreateBookingRequest req) {
        LocalDateTime now = LocalDateTime.now();

		//A booking must have a start time and an end time.
		if (req.getStartTime() != null && req.getEndTime() != null) {
			throw new BookingValidationException(EXEC_BOOKING_TIME);
		}
		
		//Bookings cannot be in the past
        if (req.getStartTime().isBefore(now)) {
            throw new BookingValidationException(EXEC_PAST_BOOKING_START_TIME);
        }

		// End Time should be after start time
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            throw new BookingValidationException(EXEC_END_TIME);
        }

		//Maximum booking duration is 2 hours.
        long minutes = Duration.between(req.getStartTime(), req.getEndTime()).toMinutes();
        if (minutes > MAX_HOURS * 60L) {
            throw new BookingValidationException(EXEC_MSG_MAX_DURATION);
        }
    }

    private BookingResponse toResponse(Booking b) {
        return BookingResponse.builder()
                .id(b.getId())
                .facilityId(b.getFacilityId())
                .userId(b.getUserId())
                .startTime(b.getStartTime())
                .endTime(b.getEndTime())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
