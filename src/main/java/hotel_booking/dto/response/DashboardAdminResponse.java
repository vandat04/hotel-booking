package hotel_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardAdminResponse {
    private List<RevenueDTO> revenueByTime;
    private List<RevenueDTO> revenueByRoomType;
    private List<RevenueDTO> revenueByMethod;

    private int totalBookings;
    private double occupancyRate;

    private double actualRevenue;
    private double expectedRevenue;

    private String topRoomType;
}
