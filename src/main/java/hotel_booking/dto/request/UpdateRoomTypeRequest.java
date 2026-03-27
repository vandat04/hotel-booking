package hotel_booking.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateRoomTypeRequest {
    private String name;
    private BigDecimal priceHour;
    private BigDecimal priceDay;
    private String description;
    private Integer status;
}
