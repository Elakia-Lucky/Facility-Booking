package facility.bookings.service;

import facility.bookings.dto.BookingResponse;
import facility.bookings.dto.CreateBookingRequest;
import facility.bookings.exception.BookingConflictException;
import facility.bookings.exception.BookingValidationException;
import facility.bookings.model.Booking;
import facility.bookings.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    private CreateBookingRequest validRequest;
    private final LocalDateTime FUTURE_START = LocalDateTime.now().plusDays(1).withNano(0);

    @BeforeEach
    void setUp() {
        validRequest = new CreateBookingRequest();
        validRequest.setFacilityId("COURT-A");
        validRequest.setUserId("elakia");
        validRequest.setStartTime(FUTURE_START);
        validRequest.setEndTime(FUTURE_START.plusHours(1));
    }

    @Test
    void testBooking() {
        Booking boo = buildBooking(123, "COURT-A", "elakia", FUTURE_START, FUTURE_START.plusHours(1));
        when(bookingRepository.findOverlapping(any(), any(), any())).thenReturn(List.of());
        when(bookingRepository.save(any())).thenReturn(boo);

        BookingResponse result = bookingService.createBooking(validRequest);

        assertThat(result.getId()).isEqualTo(123);
        assertThat(result.getFacilityId()).isEqualTo("COURT-A");
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void testStartTimeIsInThePast() {
        validRequest.setStartTime(LocalDateTime.now().minusMinutes(10));
        validRequest.setEndTime(LocalDateTime.now().plusMinutes(30));

        assertThatThrownBy(() -> bookingService.createBooking(validRequest))
                .isInstanceOf(BookingValidationException.class)
                .hasMessageContaining("past");
    }

    @Test
    void testEndTimeBeforeStartTime() {
        validRequest.setEndTime(FUTURE_START.minusMinutes(30));

        assertThatThrownBy(() -> bookingService.createBooking(validRequest))
                .isInstanceOf(BookingValidationException.class)
                .hasMessageContaining("after start time");
    }

    @Test
    void testEndTimeEqualToStartTime() {
        validRequest.setEndTime(FUTURE_START);

        assertThatThrownBy(() -> bookingService.createBooking(validRequest))
                .isInstanceOf(BookingValidationException.class)
                .hasMessageContaining("after start time");
    }

    @Test
    void testDurationExceedsTwoHours() {
        validRequest.setEndTime(FUTURE_START.plusHours(2).plusMinutes(1));

        assertThatThrownBy(() -> bookingService.createBooking(validRequest))
                .isInstanceOf(BookingValidationException.class)
                .hasMessageContaining("2 hours");
    }


    @Test
    void testGetBookingsForUser() {
        List<Booking> bookings = List.of(
                buildBooking(1L, "COURT-A", "elakia", FUTURE_START, FUTURE_START.plusHours(1)),
                buildBooking(2L, "COURT-B", "elakia", FUTURE_START.plusDays(1), FUTURE_START.plusDays(1).plusHours(1))
        );
        when(bookingRepository.findByUserIdOrderByStartTimeAsc("elakia")).thenReturn(bookings);

        List<BookingResponse> result = bookingService.getBookingsForUser("elakia");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFacilityId()).isEqualTo("COURT-A");
        assertThat(result.get(1).getFacilityId()).isEqualTo("COURT-B");
    }

    @Test
    void testGetBookingsForUser_doesNotReturnOtherUsersBookings() {
        when(bookingRepository.findByUserIdOrderByStartTimeAsc("naga")).thenReturn(List.of());

        List<BookingResponse> result = bookingService.getBookingsForUser("naga");

        assertThat(result).isEmpty();
        verify(bookingRepository).findByUserIdOrderByStartTimeAsc("naga");
    }

    private Booking buildBooking(Long id, String facilityId, String userId,
                                  LocalDateTime start, LocalDateTime end) {
        return Booking.builder()
                .id(id)
                .facilityId(facilityId)
                .userId(userId)
                .startTime(start)
                .endTime(end)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
