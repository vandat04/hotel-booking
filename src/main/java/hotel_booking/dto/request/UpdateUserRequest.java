package hotel_booking.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String role;   // ADMIN chỉ nên update role khi cần
    private Integer status; // 1: ACTIVE, 2: INACTIVE, 3: BANNED
}
