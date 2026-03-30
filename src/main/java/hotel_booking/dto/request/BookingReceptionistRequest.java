package hotel_booking.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingReceptionistRequest {
    private Long typeId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int quantity;
    private String fullName;
    private String phone;
    private String address;
}
