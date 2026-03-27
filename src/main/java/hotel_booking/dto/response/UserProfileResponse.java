package hotel_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private Integer id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String role;
    private Integer status;
    private LocalDateTime createdAt;
}
