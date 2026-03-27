package hotel_booking.service;

import hotel_booking.dto.request.CreateMultipleRoomsRequest;
import hotel_booking.dto.request.CreateRoomRequest;
import hotel_booking.dto.request.PaginationRequest;
import hotel_booking.dto.request.UpdateRoomRequest;
import hotel_booking.dto.response.RoomDetailResponse;
import hotel_booking.dto.response.RoomItemResponse;
import hotel_booking.dto.response.RoomResponse;
import hotel_booking.dto.response.RoomTypeResponse;
import hotel_booking.entity.Room;
import hotel_booking.entity.RoomItem;
import hotel_booking.entity.RoomType;
import hotel_booking.repository.RoomItemRepository;
import hotel_booking.repository.RoomRepository;
import hotel_booking.repository.RoomTypeRepository;
import hotel_booking.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomItemRepository roomItemRepository;

    // Tạo phòng đơn
    public Room createRoom(CreateRoomRequest request) {
        RoomType type = roomTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new RuntimeException("RoomType not found"));

        if (type.getStatus() != 1 ){
            throw  new RuntimeException("RoomType not active with id: " + request.getTypeId());
        }

        if (roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new RuntimeException("Room number already exists");
        }

        Room room = Room.builder()
                .roomNumber(request.getRoomNumber())
                .typeId(type.getId())
                .status("AVAILABLE")
                .build();

        return roomRepository.save(room);
    }

    // Tạo phòng hàng loạt
    public List<Room> createMultipleRooms(CreateMultipleRoomsRequest request) {
        RoomType type = roomTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new RuntimeException("RoomType not found"));

        if (type.getStatus() != 1 ){
            throw  new RuntimeException("RoomType not active with id: " + request.getTypeId());
        }

        // 1. Tìm số thứ tự cao nhất với prefix
        List<Room> existingRooms = roomRepository.findAll(); // bạn có thể filter theo type
        int maxNumber = existingRooms.stream()
                .map(Room::getRoomNumber)
                .filter(rn -> rn.startsWith(request.getRoomNumberPrefix()))
                .map(rn -> rn.substring(request.getRoomNumberPrefix().length())) // lấy phần số
                .mapToInt(s -> {
                    try {
                        return Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0); // nếu chưa có phòng nào, bắt đầu từ 0

        // 2. Tạo phòng tiếp theo
        List<Room> rooms = new ArrayList<>();
        for (int i = 1; i <= request.getQuantity(); i++) {
            int nextNumber = maxNumber + i;
            String roomNumber = String.format("%s%02d", request.getRoomNumberPrefix(), nextNumber);
            Room room = Room.builder()
                    .roomNumber(roomNumber)
                    .typeId(type.getId())
                    .status("AVAILABLE")
                    .build();
            rooms.add(room);
        }

        return roomRepository.saveAll(rooms);
    }

    public Page<RoomResponse> getRooms(
            PaginationRequest req,
            Integer typeId,
            String status
    ) {

        // default sort
        if (req.getSortBy() == null || req.getSortBy().isBlank()) {
            req.setSortBy("typeId");
            req.setDirection("asc");
        }

        Pageable pageable = PaginationUtil.build(req);

        Page<Room> result;

        // ================= FILTER =================

        if (typeId != null && status != null) {
            result = roomRepository.findByTypeIdAndStatus(typeId, status, pageable);

        } else if (typeId != null) {
            result = roomRepository.findByTypeId(typeId, pageable);

        } else if (status != null && !status.isBlank()) {
            result = roomRepository.findByStatus(status, pageable);

        } else {
            result = roomRepository.findAll(pageable);
        }

        return result.map(this::toResponse);
    }

    public Page<RoomResponse> searchRooms(
            PaginationRequest req,
            String search
    ) {

        // 🔥 default sort theo roomNumber
        if (req.getSortBy() == null || req.getSortBy().isBlank()) {
            req.setSortBy("roomNumber");
            req.setDirection("asc");
        }

        Pageable pageable = PaginationUtil.build(req);

        String keyword = (search != null) ? search.trim() : "";

        Page<Room> result;

        if (!keyword.isBlank()) {
            result = roomRepository
                    .findByRoomNumberContainingIgnoreCase(keyword, pageable);
        } else {
            result = roomRepository.findAll(pageable);
        }

        return result.map(this::toResponse);
    }

    public RoomDetailResponse getRoomDetail(Long roomId) {

        // 1. Room
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // 2. RoomType
        RoomType roomType = roomTypeRepository.findById(room.getTypeId())
                .orElseThrow(() -> new RuntimeException("RoomType not found"));

        // 3. RoomItems (chỉ lấy active)
        List<RoomItem> items = roomItemRepository
                .findByRoomTypeId(roomType.getId());

        // ================= MAP =================

        RoomDetailResponse response = new RoomDetailResponse();

        response.setId(room.getId());
        response.setRoomNumber(room.getRoomNumber());
        response.setStatus(room.getStatus());

        // 🔹 reuse RoomTypeResponse
        RoomTypeResponse typeRes = new RoomTypeResponse();
        typeRes.setId(roomType.getId());
        typeRes.setName(roomType.getName());
        typeRes.setPriceHour(roomType.getPriceHour());
        typeRes.setPriceDay(roomType.getPriceDay());
        typeRes.setDescription(roomType.getDescription());
        typeRes.setStatus(roomType.getStatus());

        response.setRoomType(typeRes);

        // 🔹 reuse RoomItemResponse
        List<RoomItemResponse> itemResponses = items.stream().map(item -> {
            RoomItemResponse dto = new RoomItemResponse();
            dto.setId(item.getId());
            dto.setRoomTypeId(item.getRoomTypeId());
            dto.setName(item.getName());
            dto.setQuantity(item.getQuantity());
            dto.setPrice(item.getPrice());
            dto.setStatus(item.getStatus());
            return dto;
        }).toList();

        response.setItems(itemResponses);

        return response;
    }

    public RoomResponse updateRoom(Long id, UpdateRoomRequest request) {

        //  1. Tìm room
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        //  2. Update roomNumber
        if (request.getRoomNumber() != null && !request.getRoomNumber().isBlank()) {

            // check trùng (trừ chính nó)
            boolean exists = roomRepository.existsByRoomNumber(request.getRoomNumber());
            if (exists && !room.getRoomNumber().equals(request.getRoomNumber())) {
                throw new RuntimeException("Room number already exists");
            }

            room.setRoomNumber(request.getRoomNumber());
        }

        // 🔥 3. Update typeId
        if (request.getTypeId() != null) {

            if (!roomTypeRepository.existsById(request.getTypeId())) {
                throw new RuntimeException("RoomType not found");
            }

            room.setTypeId(request.getTypeId());
        }

        //  4. Update status
        if (request.getStatus() != null && !request.getStatus().isBlank()) {

            // validate status
            String status = request.getStatus().toUpperCase();

            if (!status.equals("AVAILABLE") &&
                    !status.equals("OCCUPIED") &&
                    !status.equals("CLEANING") &&
                    !status.equals("MAINTENANCE")) {

                throw new IllegalArgumentException("Invalid status");
            }

            room.setStatus(status);
        }

        //  5. Save
        Room updated = roomRepository.save(room);

        return toResponse(updated);
    }

    private RoomResponse toResponse(Room room) {
        RoomResponse dto = new RoomResponse();
        dto.setId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setTypeId(room.getTypeId());
        dto.setStatus(room.getStatus());
        return dto;
    }
}
