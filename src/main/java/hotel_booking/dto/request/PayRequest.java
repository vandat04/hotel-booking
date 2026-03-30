package hotel_booking.dto.request;

import lombok.Data;

@Data
public class PayRequest {

    private Long bookingId;
    private Long paymentId;
    private String method; // CASH hoặc VNPAY
}
