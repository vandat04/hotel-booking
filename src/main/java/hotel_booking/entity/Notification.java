package hotel_booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;

    private String message;
    @Column(name = "is_read")
    private Boolean isRead;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    private String role;
}
