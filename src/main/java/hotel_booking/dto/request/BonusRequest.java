package hotel_booking.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BonusRequest {

    private Long userId; // null = thưởng tất cả
    private BigDecimal amount;
    private String note;
    private LocalDate workDate; // optional (default today)
}
