package hotel_booking.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateRoomTypeRequest {
    private String name;
    private String description;
    private BigDecimal priceHour;
    private BigDecimal priceDay;
}
