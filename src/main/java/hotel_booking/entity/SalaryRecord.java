package hotel_booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "SalaryRecords")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "work_date")
    private LocalDate workDate;
    @Column(name = "amount")
    private BigDecimal amount;
    @Column(name = "type")
    private String type;
    @Column(name = "note")
    private String note;
    @Column(name = "batch_id")
    private String batchId;
}
