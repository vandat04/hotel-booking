package hotel_booking.dto.request;
import lombok.Data;

@Data
public class ReviewRequest {
    private Long bookingId;
    private Long roomId;
    private int rating;
    private String comment;
}
