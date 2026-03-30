package hotel_booking.repository;

import hotel_booking.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBookingId(Long bookingId);

    void deleteByBookingId(Long bookingId);

    Optional<Payment> findByVnpTxnRef(String vnpTxnRef);

    boolean existsByBookingIdAndPaymentTypeAndStatus(
            Long bookingId,
            String paymentType,
            String status
    );

    Optional<Payment> findByBookingIdAndPaymentType(Long bookingId, String paymentType);

    List<Payment> findByBookingIdAndStatus(Long bookingId, String status);

    List<Payment> findByBookingIdIn(Set<Long> bookingIds);

}
