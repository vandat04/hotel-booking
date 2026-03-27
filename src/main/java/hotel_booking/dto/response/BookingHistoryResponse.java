package hotel_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class BookingHistoryResponse {

    private Long id;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String status;
    private String bookingType;
    private BigDecimal totalPrice;
    private String channel;
    private LocalDateTime createdAt;
}