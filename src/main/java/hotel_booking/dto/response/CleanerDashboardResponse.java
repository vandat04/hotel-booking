package hotel_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CleanerDashboardResponse {
    private long totalTasks;
    private long pendingTasks;
    private long inProgressTasks;
    private long doneTasks;
    private long todayTasks;
}
