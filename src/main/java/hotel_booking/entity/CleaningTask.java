package hotel_booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CleaningTasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CleaningTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "room_id")
    private Long roomId;
    @Column(name = "cleaner_id")
    private Long cleanerId;
    @Column(name = "booking_id")
    private Long bookingId;

    private String status;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
