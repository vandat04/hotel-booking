package hotel_booking.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomResponse {

    private Long id;
    private String roomNumber;
    private Long typeId;
    private String status;
}
