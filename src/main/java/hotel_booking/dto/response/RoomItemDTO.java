package hotel_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoomItemDTO {
    private Long id;
    private String roomNumber;
    private String status;
}
