package hotel_booking.service;

import hotel_booking.dto.request.CreateRoomTypeRequest;
import hotel_booking.dto.request.PaginationRequest;
import hotel_booking.dto.request.UpdateRoomTypeRequest;
import hotel_booking.dto.response.RoomItemResponse;
import hotel_booking.dto.response.RoomTypeDetailResponse;
import hotel_booking.dto.response.RoomTypeResponse;
import hotel_booking.entity.Review;
import hotel_booking.entity.Room;
import hotel_booking.entity.RoomType;
import hotel_booking.repository.*;
import hotel_booking.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomTypeService {
    private final RoomTypeRepository roomTypeRepository;
    private final RoomPictureRepository roomPictureRepository;
    private final RoomItemRepository roomItemRepository;
    private final RoomRepository roomRepository;
    private final ReviewRepository reviewRepository;


    public RoomType createRoomType(CreateRoomTypeRequest request) {
        if (roomTypeRepository.existsByName(request.getName())) {
            throw new RuntimeException("Room type already exists: " + request.getName());
        }

        RoomType roomType = RoomType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(1)
                .priceHour(request.getPriceHour())
                .priceDay(request.getPriceDay())
                .build();

        return roomTypeRepository.save(roomType);
    }

    public RoomType updateRoomType(Long id, UpdateRoomTypeRequest request) {

        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomType not found"));

        // Update từng field nếu có dữ liệu
        if (request.getName() != null) {
            roomType.setName(request.getName());
        }

        if (request.getPriceHour() != null) {
            roomType.setPriceHour(request.getPriceHour());
        }

        if (request.getPriceDay() != null) {
            roomType.setPriceDay(request.getPriceDay());
        }

        if (request.getDescription() != null) {
            roomType.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            roomType.setStatus(request.getStatus());
        }

        return roomTypeRepository.save(roomType);
    }

    public Page<RoomTypeResponse> getAllRoomTypes(
            PaginationRequest req,
            Integer status
    ) {

        // validate status
        if (status != null && status != 0 && status != 1) {
            throw new IllegalArgumentException("Status must be 0 or 1");
        }

        Pageable pageable = PaginationUtil.build(req);

        Page<RoomType> result = (status != null)
                ? roomTypeRepository.findByStatus(status, pageable)
                : roomTypeRepository.findAll(pageable);

        return result.map(this::toResponse);
    }

    public Page<RoomTypeResponse> getAllRoomTypes(
            PaginationRequest req,
            Integer status,
            String name
    ) {

        // validate status
        if (status != null && status != 0 && status != 1) {
            throw new IllegalArgumentException("Status must be 0 or 1");
        }

        // default sort A → Z nếu không truyền
        if (req.getSortBy() == null || req.getSortBy().isBlank()) {
            req.setSortBy("name");
            req.setDirection("asc");
        }

        Pageable pageable = PaginationUtil.build(req);

        Page<RoomType> result;

        //logic filter
        if (name != null && !name.isBlank() && status != null) {
            result = roomTypeRepository
                    .findByNameContainingIgnoreCaseAndStatus(name, status, pageable);

        } else if (name != null && !name.isBlank()) {
            result = roomTypeRepository
                    .findByNameContainingIgnoreCase(name, pageable);

        } else if (status != null) {
            result = roomTypeRepository
                    .findByStatus(status, pageable);

        } else {
            result = roomTypeRepository.findAll(pageable);
        }

        return result.map(this::toResponse);
    }

    public RoomTypeResponse getRoomTypeById(Integer id) {

        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomType not found"));

        return toResponse(roomType);
    }

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("id", "priceDay", "priceHour", "name");

    public Page<RoomTypeResponse> getAllActiveRoomTypes(int page, int size, String sortBy, String sortDir) {

        size = Math.min(size, 20);

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "id";
        }

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return roomTypeRepository.findByStatus(1, pageable)
                .map(this::toResponse);
    }

    private RoomTypeResponse toResponse(RoomType entity) {
        RoomTypeResponse dto = new RoomTypeResponse();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPriceHour(entity.getPriceHour());
        dto.setPriceDay(entity.getPriceDay());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    public RoomTypeDetailResponse getRoomTypeDetail(Long id) {

        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomType not found"));

        // 🔥 Images
        List<String> images = roomPictureRepository.findByRoomTypeId(id.intValue())
                .stream()
                .map(p -> p.getImageUrl())
                .toList();

        // 🔥 Items (REUSE DTO nhưng không set field thừa)
        List<RoomItemResponse> items = roomItemRepository
                .findByRoomTypeIdAndStatus(id, 1)
                .stream()
                .map(item -> {
                    RoomItemResponse dto = new RoomItemResponse();

                    dto.setName(item.getName());
                    dto.setQuantity(item.getQuantity());
                    dto.setPrice(item.getPrice());

                    return dto;
                })
                .toList();

        // 🔥 Total rooms
        int totalRooms = (int) roomRepository.countByTypeId(id);

        // 🔥 Count theo status
        Map<String, Long> statusCounts = roomRepository
                .countRoomStatusByTypeId(id)
                .stream()
                .collect(Collectors.toMap(
                        obj -> (String) obj[0],
                        obj -> (Long) obj[1]
                ));

        // 1️⃣ Lấy danh sách phòng theo typeId
        List<Room> rooms = roomRepository.findByTypeId(id);

        List<Long> roomIds = rooms.stream()
                .map(Room::getId)
                .collect(Collectors.toList());

        // 2️⃣ Lấy review theo danh sách phòng
        List<Review> reviews = reviewRepository.findByRoomIdIn(roomIds);


        // 3️⃣ Tính rating trung bình
        double avgRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // 4️⃣ Lấy danh sách comment
        List<String> comments = reviews.stream()
                .map(Review::getComment)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 🔥 Build response
        RoomTypeDetailResponse response = new RoomTypeDetailResponse();
        response.setId(roomType.getId());
        response.setName(roomType.getName());
        response.setPriceHour(roomType.getPriceHour());
        response.setPriceDay(roomType.getPriceDay());
        response.setDescription(roomType.getDescription());

        response.setImages(images);
        response.setItems(items);
        response.setTotalRooms(totalRooms);
        response.setRoomStatusCounts(statusCounts);
        response.setRate((rooms.isEmpty() || reviews.isEmpty() ) ? 0.0 : avgRating);
        response.setComment(comments);
        return response;
    }

    public Page<RoomTypeResponse> searchByPriceDay(BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<RoomType> roomPage = roomTypeRepository.findByPriceDayBetween(minPrice, maxPrice, pageable);

        return roomPage.map(this::mapToResponse);
    }

    private RoomTypeResponse mapToResponse(RoomType entity) {
        RoomTypeResponse res = new RoomTypeResponse();
        res.setId(entity.getId());
        res.setName(entity.getName());
        res.setPriceHour(entity.getPriceHour());
        res.setPriceDay(entity.getPriceDay());
        res.setDescription(entity.getDescription());
        res.setStatus(entity.getStatus());
        return res;
    }
}
