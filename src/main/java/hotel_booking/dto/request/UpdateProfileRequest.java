package hotel_booking.dto.request;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private java.time.LocalDate dob;
}
