package hotel_booking.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AttendanceResponse {

    private Integer id;
    private Long userId;
    private LocalDate workDate;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private Integer status;
    private Integer slotId;
}