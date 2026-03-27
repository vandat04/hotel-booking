package hotel_booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Rooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "room_number")
    private String roomNumber;
    @Column(name = "type_id")
    private Long typeId;

    private String status;
}