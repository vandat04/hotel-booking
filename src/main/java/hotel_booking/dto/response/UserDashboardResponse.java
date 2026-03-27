package hotel_booking.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class UserDashboardResponse {

    private Long totalUsers;

    private Long totalAdmin;
    private Long totalCleaner;
    private Long totalReceptionist;
    private Long totalCustomer;

    private Long activeUsers;
    private Long inactiveUsers;
    private Long bannedUsers;

    private List<UserProfileResponse> recentUsers;
}