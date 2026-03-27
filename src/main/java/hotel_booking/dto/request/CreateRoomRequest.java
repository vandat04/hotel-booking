package hotel_booking.dto.request;

import lombok.Data;

@Data
public class CreateRoomRequest {
    private String roomNumber;
    private Long typeId;
}
