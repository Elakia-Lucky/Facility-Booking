package facility.bookings.exception;

public class BookingConflictException extends RuntimeException {

    public BookingConflictException(String facilityId) {
        super("Facility '" + facilityId + "' is already booked for the requested time slot.");
    }
}
