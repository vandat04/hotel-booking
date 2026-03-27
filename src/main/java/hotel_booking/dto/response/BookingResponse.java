package hotel_booking.dto.response;

import lombok.Data;

@Data
public class BookingResponse {
    private Long bookingId;
    private String paymentUrl;
    private int bookingStatus;
    private int availableRooms;
}