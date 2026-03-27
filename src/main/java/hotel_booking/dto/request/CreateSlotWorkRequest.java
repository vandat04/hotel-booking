package hotel_booking.dto.request;

import lombok.Data;

import java.time.LocalTime;

@Data
public class CreateSlotWorkRequest {
    private LocalTime startTime;
    private LocalTime endTime;
}
