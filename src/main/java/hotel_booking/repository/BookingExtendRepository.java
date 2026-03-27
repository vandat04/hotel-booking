package hotel_booking.repository;

import hotel_booking.entity.BookingExtend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingExtendRepository extends JpaRepository<BookingExtend, Long> {
    List<BookingExtend> findByBookingId(Long bookingId);
}
