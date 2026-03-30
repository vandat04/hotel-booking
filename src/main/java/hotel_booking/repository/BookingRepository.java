package hotel_booking.repository;

import hotel_booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>,
        JpaSpecificationExecutor<Booking> {


    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    Optional<Booking> findById(Long id);

    List<Booking> findByIdIn(Set<Long> ids);

    @Query(value = "SELECT COUNT(*) FROM Bookings", nativeQuery = true)
    Integer countAllBookings();

    @Query(value = """
        SELECT ISNULL(SUM(total_price),0)
        FROM Bookings
        WHERE status IN ('BOOKED','CHECKED_IN')
    """, nativeQuery = true)
    Double getExpectedRevenue();

    @Query(value = """
        SELECT * FROM Bookings b
        WHERE (:status IS NULL OR b.status = :status)
          AND (:date IS NULL OR CAST(b.created_at AS DATE) = :date)
    """,
            countQuery = """
        SELECT COUNT(*) FROM Bookings b
        WHERE (:status IS NULL OR b.status = :status)
          AND (:date IS NULL OR CAST(b.created_at AS DATE) = :date)
    """,
            nativeQuery = true)
    Page<Booking> searchBookings(
            @Param("status") String status,
            @Param("date") LocalDate date,
            Pageable pageable
    );
}
