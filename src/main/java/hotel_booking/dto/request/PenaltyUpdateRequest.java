package hotel_booking.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PenaltyUpdateRequest {

    private LocalDate workDate;
    private BigDecimal amount;
    private String reason;
}