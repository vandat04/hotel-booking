package hotel_booking.controller;

import hotel_booking.dto.request.*;
import hotel_booking.entity.Room;
import hotel_booking.entity.RoomItem;
import hotel_booking.entity.RoomType;
import hotel_booking.service.RoomItemService;
import hotel_booking.service.RoomService;
import hotel_booking.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class RoomController {
    private final RoomTypeService roomTypeService;
    private final RoomItemService roomItemService;
    private final RoomService roomService;


    @PostMapping("/room-types/add")
    public ResponseEntity<RoomType> createRoomType(@RequestBody CreateRoomTypeRequest request) {
        RoomType roomType = roomTypeService.createRoomType(request);
        return ResponseEntity.ok(roomType);
    }

    @PutMapping("/room-types/{id}")
    public RoomType updateRoomType(
            @PathVariable Long id,
            @RequestBody UpdateRoomTypeRequest request
    ) {
        return roomTypeService.updateRoomType(id, request);
    }

    @GetMapping("/room-types")
    public ResponseEntity<?> getAllRoomTypes(
            @ModelAttribute PaginationRequest req,
            @RequestParam(required = false) Integer status
    ) {

        return ResponseEntity.ok(
                roomTypeService.getAllRoomTypes(req, status)
        );
    }

    @GetMapping("/room-types/search")
    public ResponseEntity<?> getAllRoomTypes(
            @ModelAttribute PaginationRequest req,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String name
    ) {

        return ResponseEntity.ok(
                roomTypeService.getAllRoomTypes(req, status, name)
        );
    }

    @GetMapping("/room-types/{id}")
    public ResponseEntity<?> getRoomTypeById(@PathVariable Integer id) {
        return ResponseEntity.ok(
                roomTypeService.getRoomTypeById(id)
        );
    }

    @PostMapping("/room-items/add")
    public ResponseEntity<List<RoomItem>> createRoomItems(@RequestBody CreateRoomItemsRequest request) {
        List<RoomItem> items = roomItemService.createRoomItems(request);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/room-items")
    public ResponseEntity<?> getAllRoomItems(
            @ModelAttribute PaginationRequest req,
            @RequestParam(required = false) Integer roomTypeId,
            @RequestParam(required = false) String name
    ) {

        return ResponseEntity.ok(
                roomItemService.getAllRoomItems(req, roomTypeId, name)
        );
    }

    @GetMapping("/room-items/{id}")
    public ResponseEntity<?> getRoomItemById(@PathVariable Integer id) {

        return ResponseEntity.ok(
                roomItemService.getRoomItemById(id)
        );
    }

    @PutMapping("/room-items/{id}")
    public ResponseEntity<?> updateRoomItem(
            @PathVariable Integer id,
            @RequestBody UpdateRoomItemRequest request
    ) {

        return ResponseEntity.ok(
                roomItemService.updateRoomItem(id, request)
        );
    }

    @GetMapping("/room-items/search")
    public ResponseEntity<?> searchRoomItems(
            @ModelAttribute PaginationRequest req,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status
    ) {

        return ResponseEntity.ok(
                roomItemService.searchRoomItems(req, name, status)
        );
    }

    @DeleteMapping("/room-items/{id}")
    public ResponseEntity<?> deleteRoomItem(@PathVariable Integer id) {

        roomItemService.deleteRoomItem(id);

        return ResponseEntity.noContent().build();
    }

    // Tạo phòng đơn
    @PostMapping("/room/add-single")
    public ResponseEntity<Room> createRoom(@RequestBody CreateRoomRequest request) {
        Room room = roomService.createRoom(request);
        return ResponseEntity.ok(room);
    }

    // Tạo phòng hàng loạt
    @PostMapping("/room/add-batch")
    public ResponseEntity<List<Room>> createMultipleRooms(@RequestBody CreateMultipleRoomsRequest request) {
        List<Room> rooms = roomService.createMultipleRooms(request);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/room")
    public ResponseEntity<?> getRooms(
            @ModelAttribute PaginationRequest req,
            @RequestParam(required = false) Integer typeId,
            @RequestParam(required = false) String status
    ) {

        return ResponseEntity.ok(
                roomService.getRooms(req, typeId, status)
        );
    }

    @GetMapping("/room/search")
    public ResponseEntity<?> searchRooms(
            @ModelAttribute PaginationRequest req,
            @RequestParam(required = false) String name
    ) {

        return ResponseEntity.ok(
                roomService.searchRooms(req, name)
        );
    }

    @GetMapping("/room/{id:\\d+}")
    public ResponseEntity<?> getRoomDetail(@PathVariable Long id) {

        return ResponseEntity.ok(
                roomService.getRoomDetail(id)
        );
    }

    @PutMapping("/room/{id:\\d+}")
    public ResponseEntity<?> updateRoom(
            @PathVariable Long id,
            @RequestBody UpdateRoomRequest request
    ) {

        return ResponseEntity.ok(
                roomService.updateRoom(id, request)
        );
    }
}
