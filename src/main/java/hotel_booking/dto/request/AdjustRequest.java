package hotel_booking.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdjustRequest {
    private Long recordId;
    private BigDecimal amount;
    private String note;
}
