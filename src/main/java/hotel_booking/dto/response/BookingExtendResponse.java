package hotel_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class BookingExtendResponse {
    private Long id;
    private LocalDateTime oldCheckOut;
    private LocalDateTime newCheckOut;
    private BigDecimal extraPrice;
}