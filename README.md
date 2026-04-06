# Facility-Booking
A RESTful API built with **Spring Boot 3.x** that lets users book a facility (meeting room, sports court, etc.) while preventing double-bookings and enforcing business rules.


## Business Rules
| A booking must have a start time and an end time.
| Booking cannot be in the past.
| Maximum booking duration is 2 hours.
| No double-booking.
| End Time must be after start Time.
