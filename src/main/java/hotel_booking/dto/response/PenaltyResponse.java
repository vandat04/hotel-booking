package hotel_booking.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PenaltyResponse {

    private Long id;
    private Long userId;
    private String username;
    private LocalDate workDate;
    private BigDecimal amount;
    private String reason;
    private LocalDateTime createdAt;
}