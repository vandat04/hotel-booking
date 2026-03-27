package hotel_booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "RoomPicture")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomPicture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ima_url")
    private String imageUrl;

    // Quan hệ ManyToOne với RoomTypes
    @ManyToOne
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;
    @Column(name = "public_id")
    private String publicId;
}
