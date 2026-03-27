package hotel_booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "RoomTypes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(name = "price_hour")
    private BigDecimal priceHour;
    @Column(name = "price_day")
    private BigDecimal priceDay;
    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private Integer status;
}
