package hotel_booking.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class RoomDetailResponse {

    private Long id;
    private String roomNumber;
    private String status;

    private RoomTypeResponse roomType;
    private List<RoomItemResponse> items;
}