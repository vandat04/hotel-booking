package hotel_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalaryBatchResponse {

    private String batchId;
    private String type;
    private BigDecimal totalAmount;
    private Long totalUsers;
    private LocalDate workDate;
}
