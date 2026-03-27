package hotel_booking.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdjustBatchRequest {
    private String batchId;
    private BigDecimal amount;
    private String note;
}
