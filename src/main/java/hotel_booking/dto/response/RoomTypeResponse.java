package hotel_booking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomTypeResponse {
    private Long id;
    private String name;
    private BigDecimal priceHour;
    private BigDecimal priceDay;
    private String description;
    private Integer status;
}
