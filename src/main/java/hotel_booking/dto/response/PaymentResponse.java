package hotel_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private BigDecimal amount;
    private String paymentType;
    private String method;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}
