package hotel_booking.dto.request;

import lombok.Data;

@Data
public class PaymentRequest {

    private Long bookingId;
    private String paymentType; // DEPOSIT / FINAL
    private String note;
}
