package hotel_booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "BookingExtend")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingExtend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "booking_id")
    private Long bookingId;
    @Column(name = "old_check_out")
    private LocalDateTime oldCheckOut;
    @Column(name = "new_check_out")
    private LocalDateTime newCheckOut;
    @Column(name = "extra_price")
    private BigDecimal extraPrice;
}
