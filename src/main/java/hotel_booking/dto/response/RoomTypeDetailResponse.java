package hotel_booking.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class RoomTypeDetailResponse {
    private Long id;
    private String name;
    private BigDecimal priceHour;
    private BigDecimal priceDay;
    private String description;

    private List<String> images;

    private List<RoomItemResponse> items;

    private int totalRooms;

    private Map<String, Long> roomStatusCounts;

    private Double rate;
    private List<String> comment;
}
