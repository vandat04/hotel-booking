package hotel_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
@AllArgsConstructor
@Builder
public class PaymentSummaryResponse {

    private Long bookingId;

    private BigDecimal totalPrice;

    private BigDecimal depositPaid;
    private BigDecimal finalPaid;
    private BigDecimal penalty;
    private BigDecimal extend;

    private BigDecimal totalPaid;
    private BigDecimal remainingAmount;

    private List<PaymentItemResponse> payments;
}
