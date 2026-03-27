package hotel_booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "Attendance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "work_date"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "work_date")
    private LocalDate workDate;
    @Column(name = "check_in")
    private LocalTime checkIn;
    @Column(name = "check_out")
    private LocalTime checkOut;

    private Integer status;
    @ManyToOne
    @JoinColumn(name = "slot_id")
    private SlotWork slot;
}