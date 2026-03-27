package hotel_booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "RoomItems")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "room_type_id")
    private Long roomTypeId;

    private String name;
    private Integer quantity;
    private BigDecimal price;
    private Integer status;
}
