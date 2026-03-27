package hotel_booking.dto.response;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class BookingDetailResponse {

    private BookingHistoryResponse booking;

    private List<RoomKeyResponse> roomKeys;
    private List<BookingExtendResponse> extendsList;
    private List<PaymentResponse> payments;
}