package hotel_booking.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateRoomItemsRequest {

    private Long roomTypeId;

    private List<ItemRequest> items;

    @Data
    public static class ItemRequest {
        private String name;
        private Integer quantity;
        private BigDecimal price;
    }
}
