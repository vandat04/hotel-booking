package hotel_booking.dto.request;

import lombok.Data;

@Data
public class CreateMultipleRoomsRequest {
    private Long typeId;
    private Integer quantity;
    private String roomNumberPrefix;
}
