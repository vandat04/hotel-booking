package hotel_booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "RoomSchedules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "room_id")
    private Long roomId;
    @Column(name = "start_time")
    private LocalDateTime startTime;
    @Column(name = "end_time")
    private LocalDateTime endTime;
    @Column(name = "booking_id")
    private Long bookingId;

    private String status;
}
