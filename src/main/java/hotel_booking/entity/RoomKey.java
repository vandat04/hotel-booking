package hotel_booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "RoomKey")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "booking_id")
    private Long bookingId;
    @Column(name = "qr_code")
    private String qrCode;
    @Column(name = "number_code")
    private String numberCode;

    private Integer status;
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
    @Column(name = "room_id")
    private Long roomId;
}