package hotel_booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Reviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "booking_id")
    private Long bookingId;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "room_id")
    private Long roomId;

    private Integer rating;

    private String comment;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
