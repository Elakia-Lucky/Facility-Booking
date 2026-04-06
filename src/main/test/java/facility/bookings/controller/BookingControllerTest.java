package facility.bookings.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import facility.bookings.dto.BookingResponse;
import facility.bookings.dto.CreateBookingRequest;
import facility.bookings.exception.BookingConflictException;
import facility.bookings.exception.BookingValidationException;
import facility.bookings.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private final LocalDateTime FUTURE_START = LocalDateTime.now().plusDays(1).withNano(0);
    private CreateBookingRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new CreateBookingRequest();
        validRequest.setFacilityId("COURT-A");
        validRequest.setUserId("elakia");
        validRequest.setStartTime(FUTURE_START);
        validRequest.setEndTime(FUTURE_START.plusHours(1));
    }

    @Test
    void testSuccess() throws Exception {
        BookingResponse response = BookingResponse.builder()
                .id(1L)
                .facilityId("COURT-A")
                .userId("elakia")
                .startTime(FUTURE_START)
                .endTime(FUTURE_START.plusHours(1))
                .createdAt(LocalDateTime.now())
                .build();

        when(bookingService.createBooking(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.facilityId").value("COURT-A"));
    }

    @Test
    void testFacilityIdMissing() throws Exception {
        validRequest.setFacilityId("");

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testStartTimeMissing() throws Exception {
        validRequest.setStartTime(null);

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testEndTimeMissing() throws Exception {
        validRequest.setEndTime(null);

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testConflict() throws Exception {
        when(bookingService.createBooking(any()))
                .thenThrow(new BookingConflictException("COURT-A"));

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void testBookings() throws Exception {
        List<BookingResponse> bookings = List.of(
                BookingResponse.builder()
                        .id(1L).facilityId("COURT-A").userId("elakia")
                        .startTime(FUTURE_START).endTime(FUTURE_START.plusHours(1))
                        .createdAt(LocalDateTime.now())
                        .build()
        );
        when(bookingService.getBookingsForUser("elakia")).thenReturn(bookings);

        mockMvc.perform(get("/api/v1/bookings").param("userId", "elakia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].facilityId").value("COURT-A"));
    }

    @Test
    void testNoBookings() throws Exception {
        when(bookingService.getBookingsForUser("nagarajan")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/bookings").param("userId", "nagarajan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testUserIdMissing() throws Exception {
        mockMvc.perform(get("/api/v1/bookings"))
                .andExpect(status().isBadRequest());
    }
}
