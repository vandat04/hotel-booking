package hotel_booking.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomItemResponse {
    private Long id;
    private Long roomTypeId;
    private String name;
    private Integer quantity;
    private BigDecimal price;
    private Integer status;
}
