package hotel_booking.dto.request;

import lombok.Data;

@Data
public class BookingExtendRequest {
    private Long bookingId;
    private int extraDays;
}