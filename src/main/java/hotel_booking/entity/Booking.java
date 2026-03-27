package hotel_booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "Bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "check_in")
    private LocalDateTime checkIn;
    @Column(name = "check_out")
    private LocalDateTime checkOut;
    @Column(name = "booking_type")
    private String bookingType;
    private String status;

    private String source;
    private String channel;
    @Column(name = "total_price")
    private BigDecimal totalPrice;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private String note;
    private Integer quantity;
}