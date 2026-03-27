package hotel_booking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtaBookingRequest {

    // Thông tin khách
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private Integer guestCount; // số lượng khách, có thể lưu tạm vào Bookings.note nếu muốn

    // Thông tin booking
    private String otaBookingId; // id của booking bên OTA
    private String otaName;      // tên OTA (Agoda, Booking, v.v.)
    private String bookingType;  // HOUR / DAY
    private String note;
    private String channel;      // WALKIN, WEBSITE, OTA
    private String source;       // DIRECT, OTA
    private String checkIn;      // ISO datetime string
    private String checkOut;     // ISO datetime string
    private Long roomTypeId; // map với RoomTypes.id
    // Danh sách phòng
    private List<OtaRoomInfo> rooms;

    // Thanh toán
    private BigDecimal totalPrice;
    private String paymentMethod;   // CASH, VNPAY, v.v.
    private String paymentStatus;   // PENDING, PAID, FAILED
    private String transactionCode; // mã giao dịch nếu có
}
