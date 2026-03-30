package hotel_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class RevenueDashboardResponse {

    private Long typeId;
    private String typeName;

    private BigDecimal expectedRevenue;
    private BigDecimal actualRevenue;

    private Map<String, BigDecimal> revenueByMethod;
    private Map<String, BigDecimal> revenueByStatus;
}
