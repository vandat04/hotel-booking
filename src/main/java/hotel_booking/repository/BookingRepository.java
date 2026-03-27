package hotel_booking.repository;

import hotel_booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    // tất cả lịch sử
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    // filter theo 1 status
    List<Booking> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    // filter nhiều status (dùng cho UI tab)
    List<Booking> findByUserIdAndStatusInOrderByCreatedAtDesc(Long userId, List<String> statuses);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);
}
