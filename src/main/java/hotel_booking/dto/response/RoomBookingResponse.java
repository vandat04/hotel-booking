package hotel_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomBookingResponse {
    private Long bookingId;
    private Long roomId;
    private String roomNumber; // nếu bạn muốn hiển thị
}
