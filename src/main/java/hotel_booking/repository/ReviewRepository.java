package hotel_booking.repository;

import hotel_booking.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByBookingId(Long bookingId);

    boolean existsByBookingIdAndRoomId(Long bookingId, Long roomId);

    List<Review> findByRoomIdIn(List<Long> roomIds);
}