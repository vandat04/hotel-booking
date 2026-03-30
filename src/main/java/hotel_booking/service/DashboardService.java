package hotel_booking.service;

import hotel_booking.dto.response.CleanerDashboardResponse;
import hotel_booking.dto.response.RoomItemDTO;
import hotel_booking.dto.response.RoomTypeDashboardDTO;
import hotel_booking.entity.Room;
import hotel_booking.entity.RoomSchedule;
import hotel_booking.entity.RoomType;
import hotel_booking.repository.CleaningTaskRepository;
import hotel_booking.repository.RoomRepository;
import hotel_booking.repository.RoomScheduleRepository;
import hotel_booking.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final RoomScheduleRepository roomScheduleRepository;

    public List<RoomTypeDashboardDTO> getDashboard() {

        List<RoomType> types = roomTypeRepository.findAll();
        List<RoomTypeDashboardDTO> result = new ArrayList<>();

        for (RoomType type : types) {

            List<Room> rooms = roomRepository.findByTypeId(type.getId());

            List<RoomItemDTO> availableRooms = new ArrayList<>();
            List<RoomItemDTO> occupiedRooms = new ArrayList<>();

            int available = 0;
            int occupied = 0;
            int booked = 0;
            int cleaning = 0;
            int maintenance = 0;

            for (Room r : rooms) {

                // 🔥 tìm schedule hiện tại
                RoomSchedule rs = roomScheduleRepository
                        .findCurrentByRoomId(r.getId())
                        .orElse(null);

                String status;

                if (rs != null) {
                    status = rs.getStatus(); // BOOKED / OCCUPIED
                } else {
                    status = r.getStatus(); // AVAILABLE / CLEANING / MAINTENANCE
                }

                switch (status) {
                    case "AVAILABLE" -> {
                        available++;
                        availableRooms.add(new RoomItemDTO(r.getId(), r.getRoomNumber(), status));
                    }
                    case "OCCUPIED" -> {
                        occupied++;
                        occupiedRooms.add(new RoomItemDTO(r.getId(), r.getRoomNumber(), status));
                    }
                    case "BOOKED" -> booked++;
                    case "CLEANING" -> cleaning++;
                    case "MAINTENANCE" -> maintenance++;
                }
            }

            RoomTypeDashboardDTO dto = new RoomTypeDashboardDTO();
            dto.setTypeId(type.getId());
            dto.setTypeName(type.getName());
            dto.setTotalRooms(rooms.size());
            dto.setAvailable(available);
            dto.setOccupied(occupied);
            dto.setBooked(booked);
            dto.setCleaning(cleaning);
            dto.setMaintenance(maintenance);
            dto.setRemaining(available);

            dto.setAvailableRooms(availableRooms);
            dto.setOccupiedRooms(occupiedRooms);

            result.add(dto);
        }

        return result;
    }

    public RoomTypeDashboardDTO getReceptionistDashboard(Long typeId) {

        RoomType type;

        // 🔥 CASE 1: không truyền → lấy type đầu tiên
        if (typeId == null) {
            type = roomTypeRepository.findAll(Sort.by("id"))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No room type found"));
        }

        // 🔥 CASE 2: có truyền
        else {
            type = roomTypeRepository.findById(typeId)
                    .orElseThrow(() -> new RuntimeException("RoomType not found"));
        }

        List<Room> rooms = roomRepository.findByTypeId(type.getId());

        List<RoomItemDTO> availableRooms = new ArrayList<>();
        List<RoomItemDTO> occupiedRooms = new ArrayList<>();

        int available = 0;
        int occupied = 0;
        int booked = 0;
        int cleaning = 0;
        int maintenance = 0;

        for (Room r : rooms) {

            RoomSchedule rs = roomScheduleRepository
                    .findCurrentByRoomId(r.getId())
                    .orElse(null);

            String status = (rs != null) ? rs.getStatus() : r.getStatus();

            switch (status) {
                case "AVAILABLE" -> {
                    available++;
                    availableRooms.add(new RoomItemDTO(r.getId(), r.getRoomNumber(), status));
                }
                case "OCCUPIED" -> {
                    occupied++;
                    occupiedRooms.add(new RoomItemDTO(r.getId(), r.getRoomNumber(), status));
                }
                case "BOOKED" -> booked++;
                case "CLEANING" -> cleaning++;
                case "MAINTENANCE" -> maintenance++;
            }
        }

        RoomTypeDashboardDTO dto = new RoomTypeDashboardDTO();

        dto.setTypeId(type.getId());
        dto.setTypeName(type.getName());
        dto.setTotalRooms(rooms.size());
        dto.setAvailable(available);
        dto.setOccupied(occupied);
        dto.setBooked(booked);
        dto.setCleaning(cleaning);
        dto.setMaintenance(maintenance);
        dto.setRemaining(available);

        dto.setAvailableRooms(availableRooms);
        dto.setOccupiedRooms(occupiedRooms);

        return dto;
    }

    @Autowired
    private CleaningTaskRepository cleanTaskRepository;

    public CleanerDashboardResponse getCleanerDashboard(Long cleanerId) {

        long total = cleanTaskRepository.countByCleanerId(cleanerId);

        long pending = cleanTaskRepository
                .countByCleanerIdAndStatus(cleanerId, "PENDING");

        long inProgress = cleanTaskRepository
                .countByCleanerIdAndStatus(cleanerId, "DOING");

        long done = cleanTaskRepository
                .countByCleanerIdAndStatus(cleanerId, "DONE");

        // 🔥 task hôm nay
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        long today = cleanTaskRepository
                .countByCleanerIdAndCreatedAtBetween(
                        cleanerId,
                        startOfDay,
                        endOfDay
                );

        return new CleanerDashboardResponse(
                total,
                pending,
                inProgress,
                done,
                today
        );
    }

}
