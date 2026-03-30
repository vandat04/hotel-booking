package hotel_booking.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingDTO {
    private Integer id;
    private Integer userId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String bookingType;
    private String status;
    private String source;
    private String channel;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private String note;
    private Integer quantity;
}
