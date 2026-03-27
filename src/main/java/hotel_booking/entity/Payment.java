package hotel_booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "booking_id")
    private Long bookingId;

    private BigDecimal amount;
    @Column(name = "payment_type")
    private String paymentType;
    private String source;
    private String method;
    private String status;
    @Column(name = "transaction_code")
    private String transactionCode;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    private String note;
    @Column(name = "vnp_TxnRef")
    private String vnpTxnRef;
}
