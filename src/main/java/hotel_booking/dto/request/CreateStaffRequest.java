package hotel_booking.dto.request;

import lombok.Data;

@Data
public class CreateStaffRequest {

    private String username;
    private String password;
    private String email;
    private String fullName;
    private String phone;

    // chỉ cho phép CLEANER / RECEPTIONIST
    private String role;
}