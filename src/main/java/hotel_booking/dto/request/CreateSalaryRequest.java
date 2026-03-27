package hotel_booking.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateSalaryRequest {

    private String role;
    private BigDecimal salary;
}