package hotel_booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "CheckList")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cleaning_task_id")
    private Long cleaningTaskId;
    @Column(name = "item_id")
    private Long itemId;
    @Column(name = "expected_quantity")
    private Integer expectedQuantity;
    @Column(name = "actual_quantity")
    private Integer actualQuantity;

    private BigDecimal amount;
}
