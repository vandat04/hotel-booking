package hotel_booking.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class RoomTypeDashboardDTO {
    private Long typeId;
    private String typeName;

    private int totalRooms;
    private int available;
    private int occupied;
    private int booked;
    private int cleaning;
    private int maintenance;
    private int remaining;

    private List<RoomItemDTO> availableRooms;
    private List<RoomItemDTO> occupiedRooms;
}