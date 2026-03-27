package hotel_booking.dto.request;

import lombok.Data;

@Data
public class UpdateRoomRequest {

    private String roomNumber;
    private Long typeId;
    private String status;
}
