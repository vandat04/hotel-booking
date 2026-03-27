package hotel_booking.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateRoomItemRequest {

    private String name;
    private Integer quantity;
    private BigDecimal price;
    private Long roomTypeId;
    private Integer status;
}
