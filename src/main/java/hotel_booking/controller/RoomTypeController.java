package hotel_booking.controller;

import hotel_booking.dto.response.RoomTypeDetailResponse;
import hotel_booking.dto.response.RoomTypeResponse;
import hotel_booking.service.RoomTypeService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/hotel")
public class RoomTypeController {
    private final RoomTypeService roomTypeService;

    public RoomTypeController(RoomTypeService roomTypeService) {
        this.roomTypeService = roomTypeService;
    }

    @GetMapping("/room-types")
    public Page<RoomTypeResponse> getRoomTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return roomTypeService.getAllActiveRoomTypes(page, size, sortBy, sortDir);
    }

    @GetMapping("/room-types/search-by-day-price")
    public Page<RoomTypeResponse> searchByPriceDay(
            @RequestParam(required = false, defaultValue = "0") BigDecimal minPrice,
            @RequestParam(required = false, defaultValue = "10000000") BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return roomTypeService.searchByPriceDay(minPrice, maxPrice, page, size);
    }

    @GetMapping("/room-types/{id}")
    public RoomTypeDetailResponse getRoomTypeDetail(@PathVariable Long id) {
        return roomTypeService.getRoomTypeDetail(id);
    }


}
