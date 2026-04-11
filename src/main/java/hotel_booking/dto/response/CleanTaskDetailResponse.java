package hotel_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class CleanTaskDetailResponse {
    private Long id;
    private Long roomId;
    private String status;
    private Long cleanerId;
    private LocalDateTime createdAt;

    private Long bookingId;
    private String bookingStatus;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;

    public CleanTaskDetailResponse() {

    }
}
