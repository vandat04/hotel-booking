package hotel_booking.controller;

import hotel_booking.dto.request.CheckInRequest;
import hotel_booking.dto.request.CheckOutRequest;
import hotel_booking.service.AttendanceService;
import hotel_booking.service.SlotWorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/staff/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final SlotWorkService slotWorkService;

    @GetMapping("/slots")
    public ResponseEntity<?> getSlots() {
        return ResponseEntity.ok(slotWorkService.getAll());
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodayAttendance() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(attendanceService.getAttendanceByUser(userId, LocalDate.now(), 0, 10));
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LocalDate workDate = (date != null) ? LocalDate.parse(date) : null;
        return ResponseEntity.ok(attendanceService.getAttendanceByUser(userId, workDate, page, size));
    }

    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(@RequestBody CheckInRequest request) {
        return ResponseEntity.ok(attendanceService.checkIn(request));
    }

    @PostMapping("/check-out")
    public ResponseEntity<?> checkOut(@RequestBody CheckOutRequest request) {
        return ResponseEntity.ok(attendanceService.checkOut(request));
    }
}
