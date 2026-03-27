package hotel_booking.dto.response;

import lombok.Data;

import java.time.LocalTime;

@Data
public class SlotWorkResponse {
    private Integer id;
    private LocalTime startTime;
    private LocalTime endTime;
}
