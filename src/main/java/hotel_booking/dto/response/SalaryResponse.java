package hotel_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalaryResponse {

    private Long userId;
    private BigDecimal totalSalary;
    private Integer month;
    private Integer year;
    private Integer status;
    private Integer attendance;
}