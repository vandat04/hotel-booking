package hotel_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentItemResponse {

    private Long id;
    private BigDecimal amount;
    private String paymentType;
    private String method;
    private String status;
    private String createdAt;
}
