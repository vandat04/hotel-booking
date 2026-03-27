package hotel_booking.service;

import hotel_booking.dto.request.PaymentRequest;
import hotel_booking.entity.Booking;
import hotel_booking.entity.Payment;
import hotel_booking.repository.BookingRepository;
import hotel_booking.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final VNPayService vnPayService;

    public String createVnPayPayment(Long userId, PaymentRequest req) {

        Booking booking = bookingRepository.findById(req.getBookingId())
                .orElseThrow(() -> new RuntimeException("BOOKING_NOT_FOUND"));

        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("FORBIDDEN");
        }

        String type = req.getPaymentType();

        // 🔥 lấy payment có sẵn
        Payment payment = paymentRepository
                .findByBookingIdAndPaymentType(booking.getId(), type)
                .orElseThrow(() -> new RuntimeException("PAYMENT_NOT_FOUND"));

        // 🔥 chặn nếu đã thanh toán rồi
        if ("PAID".equals(payment.getStatus())) {
            throw new RuntimeException("PAYMENT_ALREADY_DONE");
        }

        // 🔥 generate txnRef mới mỗi lần thanh toán
        String txnRef = String.valueOf(System.currentTimeMillis());

        payment.setVnpTxnRef(txnRef);
        payment.setMethod("VNPAY");
        payment.setStatus("PENDING"); // reset nếu retry
        payment.setCreatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        return vnPayService.createPaymentUrl(payment);
    }

    public void handleVnPayReturn(Map<String, String> params) {

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        Payment payment = paymentRepository.findByVnpTxnRef(txnRef)
                .orElseThrow(() -> new RuntimeException("PAYMENT_NOT_FOUND"));

        // 🔥 chống callback nhiều lần
        if ("PAID".equals(payment.getStatus())) return;

        // 🔐 validate chữ ký
        boolean valid = vnPayService.validateSignature(params);
        if (!valid) throw new RuntimeException("INVALID_SIGNATURE");

        // 🔥 check amount
        String vnpAmount = params.get("vnp_Amount");
        String expectedAmount = payment.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .toBigInteger()
                .toString();

        if (!expectedAmount.equals(vnpAmount)) {
            throw new RuntimeException("INVALID_AMOUNT");
        }

        if ("00".equals(responseCode)) {

            payment.setStatus("PAID");
            payment.setPaidAt(LocalDateTime.now());
            payment.setTransactionCode(params.get("vnp_TransactionNo"));

            Booking booking = bookingRepository.findById(payment.getBookingId())
                    .orElseThrow();

            // 🔥 logic chuẩn theo loại payment
            if ("DEPOSIT".equals(payment.getPaymentType())) {
                booking.setStatus("CONFIRMED");
            }

            if ("FINAL".equals(payment.getPaymentType())) {
                // có thể check đã đủ tiền chưa
                booking.setStatus("FINISHED");
            }

            bookingRepository.save(booking);

        } else {
            payment.setStatus("FAILED");
        }

        paymentRepository.save(payment);
    }
}