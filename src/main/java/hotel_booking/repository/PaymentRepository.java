package hotel_booking.repository;

import hotel_booking.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    @Query(value = """
                SELECT CAST(paid_at AS DATE), SUM(amount)
                FROM Payments
                WHERE status = 'PAID'
                  AND paid_at IS NOT NULL
                GROUP BY CAST(paid_at AS DATE)
                ORDER BY 1
            """, nativeQuery = true)
    List<Object[]> getRevenueByDate();

    @Query(value = """
                SELECT method, SUM(amount)
                FROM Payments
                WHERE status = 'PAID'
                GROUP BY method
            """, nativeQuery = true)
    List<Object[]> getRevenueByMethod();

    @Query(value = """
                SELECT ISNULL(SUM(amount),0)
                FROM Payments
                WHERE status = 'PAID'
            """, nativeQuery = true)
    Double getActualRevenue();

    @Query(value = """
                SELECT rt.name, SUM(p.amount)
                FROM Payments p
                JOIN Bookings b ON p.booking_id = b.id
                JOIN RoomSchedules rs ON rs.booking_id = b.id
                JOIN Rooms r ON rs.room_id = r.id
                JOIN RoomTypes rt ON r.type_id = rt.id
                WHERE p.status = 'PAID'
                  AND p.paid_at IS NOT NULL
                GROUP BY rt.name
                ORDER BY SUM(p.amount) DESC
            """, nativeQuery = true)
    List<Object[]> getRevenueByRoomType();

    @Query(value = """
                SELECT * FROM Payments p
                WHERE (:bookingId IS NULL OR p.booking_id = :bookingId)
                  AND (:method IS NULL OR p.method = :method)
                  AND (:status IS NULL OR p.status = :status)
                  AND (
                        :date IS NULL OR 
                        CAST(p.paid_at AS DATE) = :date OR 
                        (p.paid_at IS NULL AND CAST(p.created_at AS DATE) = :date)
                      )
            """,
            countQuery = """
                        SELECT COUNT(*) FROM Payments p
                        WHERE (:bookingId IS NULL OR p.booking_id = :bookingId)
                          AND (:method IS NULL OR p.method = :method)
                          AND (:status IS NULL OR p.status = :status)
                          AND (
                                :date IS NULL OR 
                                CAST(p.paid_at AS DATE) = :date OR 
                                (p.paid_at IS NULL AND CAST(p.created_at AS DATE) = :date)
                              )
                    """,
            nativeQuery = true)
    Page<Payment> searchPayments(
            @Param("bookingId") Integer bookingId,
            @Param("method") String method,
            @Param("status") String status,
            @Param("date") LocalDate date,
            Pageable pageable
    );

    @Query(value = """
        SELECT p.payment_type, SUM(p.amount)
        FROM Payments p
        WHERE (:method IS NULL OR p.method = :method)
          AND (:status IS NULL OR p.status = :status)
          AND (
                :date IS NULL OR 
                CAST(p.paid_at AS DATE) = :date OR
                (p.paid_at IS NULL AND CAST(p.created_at AS DATE) = :date)
              )
        GROUP BY p.payment_type
    """, nativeQuery = true)
    List<Object[]> statisticByPaymentType(
            @Param("method") String method,
            @Param("status") String status,
            @Param("date") LocalDate date
    );
}
