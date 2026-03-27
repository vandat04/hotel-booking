package hotel_booking.service;

import hotel_booking.dto.request.CreateRoomItemsRequest;
import hotel_booking.dto.request.PaginationRequest;
import hotel_booking.dto.request.UpdateRoomItemRequest;
import hotel_booking.dto.response.RoomItemResponse;
import hotel_booking.entity.RoomItem;
import hotel_booking.entity.RoomType;
import hotel_booking.repository.CheckListRepository;
import hotel_booking.repository.RoomItemRepository;
import hotel_booking.repository.RoomTypeRepository;
import hotel_booking.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomItemService {

    private final RoomItemRepository roomItemRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final CheckListRepository checkListRepository;

    // ================= CREATE =================
    public List<RoomItem> createRoomItems(CreateRoomItemsRequest request) {

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new RuntimeException("RoomType not found"));

        if (roomType.getStatus() != 1) {
            throw new RuntimeException("RoomType is not active");
        }

        List<RoomItem> items = request.getItems().stream()
                .map(i -> RoomItem.builder()
                        .name(i.getName().trim())
                        .quantity(i.getQuantity())
                        .price(i.getPrice())
                        .roomTypeId(roomType.getId())
                        .status(1) // 👈 default active
                        .build())
                .toList();

        return roomItemRepository.saveAll(items);
    }

    // ================= LIST + SEARCH =================
    public Page<RoomItemResponse> getAllRoomItems(
            PaginationRequest req,
            Integer roomTypeId,
            String name
    ) {

        // default sort A → Z
        if (req.getSortBy() == null || req.getSortBy().isBlank()) {
            req.setSortBy("name");
            req.setDirection("asc");
        }

        Pageable pageable = PaginationUtil.build(req);

        Page<RoomItem> result;

        if (roomTypeId != null && name != null && !name.isBlank()) {
            result = roomItemRepository
                    .findByRoomTypeIdAndNameContainingIgnoreCaseAndStatus(
                            roomTypeId, name.trim(), 1, pageable);

        } else if (roomTypeId != null) {
            result = roomItemRepository
                    .findByRoomTypeIdAndStatus(roomTypeId, 1, pageable);

        } else if (name != null && !name.isBlank()) {
            result = roomItemRepository
                    .findByNameContainingIgnoreCaseAndStatus(name.trim(), 1, pageable);

        } else {
            result = roomItemRepository.findByStatus(1, pageable);
        }

        return result.map(this::toResponse);
    }

    public Page<RoomItemResponse> searchRoomItems(
            PaginationRequest req,
            String name,
            Integer status
    ) {

        // default sort A → Z
        if (req.getSortBy() == null || req.getSortBy().isBlank()) {
            req.setSortBy("name");
            req.setDirection("asc");
        }

        // validate nếu có truyền status
        if (status != null && status != 0 && status != 1) {
            throw new IllegalArgumentException("Status must be 0 or 1");
        }

        Pageable pageable = PaginationUtil.build(req);

        String keyword = (name != null) ? name.trim() : "";

        Page<RoomItem> result;

        // ================= LOGIC =================
        if (!keyword.isBlank() && status != null) {
            // search + filter status
            result = roomItemRepository
                    .findByNameContainingIgnoreCaseAndStatus(
                            keyword, status, pageable
                    );

        } else if (!keyword.isBlank()) {
            // search tất cả (không filter status)
            result = roomItemRepository
                    .findByNameContainingIgnoreCase(keyword, pageable);

        } else if (status != null) {
            // filter status
            result = roomItemRepository
                    .findByStatus(status, pageable);

        } else {
            // lấy tất cả
            result = roomItemRepository.findAll(pageable);
        }

        return result.map(this::toResponse);
    }

    // ================= DETAIL =================
    public RoomItemResponse getRoomItemById(Integer id) {

        RoomItem item = roomItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomItem not found"));

        return toResponse(item);
    }

    // ================= UPDATE =================
    public RoomItemResponse updateRoomItem(Integer id, UpdateRoomItemRequest request) {

        RoomItem item = roomItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomItem not found"));

        if (request.getRoomTypeId() != null) {
            RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                    .orElseThrow(() -> new RuntimeException("RoomType not found"));

            if (roomType.getStatus() != 1) {
                throw new RuntimeException("RoomType is not active");
            }

            item.setRoomTypeId(roomType.getId());
        }

        if (request.getName() != null) {
            item.setName(request.getName().trim());
        }

        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }

        if (request.getQuantity() != null) {
            if (request.getQuantity() < 0) {
                throw new IllegalArgumentException("Quantity must be >= 0");
            }
            item.setQuantity(request.getQuantity());
        }

        if (request.getPrice() != null) {
            if (request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Price must be >= 0");
            }
            item.setPrice(request.getPrice());
        }

        return toResponse(roomItemRepository.save(item));
    }

    // ================= DELETE (SOFT) =================
    public void deleteRoomItem(Integer id) {

        RoomItem item = roomItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomItem not found"));

        boolean isUsed = checkListRepository.existsByItemId(id);

        if (isUsed) {
            // 🔥 đã dùng → soft delete
            item.setStatus(0);
            roomItemRepository.save(item);

        } else {
            // 🔥 chưa dùng → hard delete
            roomItemRepository.delete(item);
        }
    }

    // ================= MAPPER =================
    private RoomItemResponse toResponse(RoomItem entity) {
        RoomItemResponse dto = new RoomItemResponse();
        dto.setId(entity.getId());
        dto.setRoomTypeId(entity.getRoomTypeId());
        dto.setName(entity.getName());
        dto.setQuantity(entity.getQuantity());
        dto.setPrice(entity.getPrice());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}