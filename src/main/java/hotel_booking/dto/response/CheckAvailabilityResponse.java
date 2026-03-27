package hotel_booking.dto.response;

import lombok.Data;

@Data
public class CheckAvailabilityResponse {

    private int bookingStatus; // 1: OK, 0: NOT OK
    private int availableRooms;
}