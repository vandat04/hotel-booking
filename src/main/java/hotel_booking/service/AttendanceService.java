package hotel_booking.service;


import hotel_booking.dto.request.CheckInRequest;
import hotel_booking.dto.request.CheckOutRequest;
import hotel_booking.dto.response.AttendanceResponse;
import hotel_booking.entity.Attendance;
import hotel_booking.entity.Penalty;
import hotel_booking.entity.SlotWork;
import hotel_booking.entity.User;
import hotel_booking.repository.AttendanceRepository;
import hotel_booking.repository.PenaltyRepository;
import hotel_booking.repository.SlotWorkRepository;
import hotel_booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SlotWorkRepository slotWorkRepository;
    private final PenaltyRepository penaltyRepository;
    private final UserRepository userRepository;

    private static final BigDecimal PENALTY_AMOUNT = BigDecimal.valueOf(100000);

    // ================= CHECK-IN =================
    public AttendanceResponse checkIn(CheckInRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 🔥 lấy userId từ principal (tuỳ bạn set trong JWT)
        Long userId = Long.parseLong(auth.getName());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // check đã check-in chưa
        if (attendanceRepository.findByUserIdAndWorkDate(user.getId(), today).isPresent()) {
            throw new RuntimeException("Already checked in today");
        }

        SlotWork slot = slotWorkRepository.findById(request.getSlotId())
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        // tạo attendance
        Attendance attendance = Attendance.builder()
                .userId(user.getId().longValue())
                .workDate(today)
                .checkIn(now)
                .status(1)
                .slot(slot)
                .build();

        // ❌ check đi trễ
        if (now.isAfter(slot.getStartTime())) {

            penaltyRepository.save(
                    Penalty.builder()
                            .userId(user.getId().longValue())
                            .workDate(today)
                            .amount(PENALTY_AMOUNT)
                            .reason("Late check-in")
                            .createdAt(LocalDateTime.now())
                            .build()
            );
            attendance.setStatus(3); //Đi trễ
        }

        return toResponse(attendanceRepository.save(attendance));
    }

    // ================= CHECK-OUT =================
    public AttendanceResponse checkOut(CheckOutRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 🔥 lấy userId từ principal (tuỳ bạn set trong JWT)
        Long userId = Long.parseLong(auth.getName());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Attendance attendance = attendanceRepository
                .findByUserIdAndWorkDate(user.getId(), today)
                .orElseThrow(() -> new RuntimeException("You have not checked in yet"));

        if (attendance.getCheckOut() != null) {
            throw new RuntimeException("Already checked out");
        }

        SlotWork slot = attendance.getSlot();

        attendance.setCheckOut(now);

        // ❌ check về sớm
        if (now.isBefore(slot.getEndTime())) {

            penaltyRepository.save(
                    Penalty.builder()
                            .userId(user.getId().longValue())
                            .workDate(today)
                            .amount(PENALTY_AMOUNT)
                            .reason("Early check-out")
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        }

        return toResponse(attendanceRepository.save(attendance));
    }

    // ================= MAPPING =================
    private AttendanceResponse toResponse(Attendance entity) {

        AttendanceResponse res = new AttendanceResponse();
        res.setId(entity.getId().intValue());
        res.setUserId(entity.getId());
        res.setWorkDate(entity.getWorkDate());
        res.setCheckIn(entity.getCheckIn());
        res.setCheckOut(entity.getCheckOut());
        res.setStatus(entity.getStatus());
        res.setSlotId(entity.getSlot().getId());

        return res;
    }

    public Page<Attendance> getAttendance(
            LocalDate date,
            Integer slotId,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("work_date").descending()
                        .and(Sort.by("check_in").descending())
        );

        return attendanceRepository.searchAttendance(date, slotId, pageable);
    }

}
