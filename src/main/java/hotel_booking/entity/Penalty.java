package hotel_booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Penalties")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Penalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "work_date")
    private LocalDate workDate;

    private BigDecimal amount;

    private String reason;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
