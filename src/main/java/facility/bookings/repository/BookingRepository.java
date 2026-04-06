package facility.bookings.repository;

import facility.bookings.model.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByStartTimeAsc(String userId);

    /**
     * Conflict Handling: The system must prevent "double-booking." 
	 * Two bookings for the same facility cannot overlap.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b FROM Booking b
            WHERE b.facilityId = :facilityId
              AND b.startTime  < :endTime
              AND b.endTime    > :startTime
            """)
    List<Booking> findOverlapping(
            @Param("facilityId") String facilityId,
            @Param("startTime")  LocalDateTime startTime,
            @Param("endTime")    LocalDateTime endTime
    );
}
