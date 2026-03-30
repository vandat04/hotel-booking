package hotel_booking.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingSearchRequest {
    private Integer userId;
    private String roomNumber;
    private String channel;
    private LocalDate date; // tìm theo ngày (check_in hoặc check_out)
}
