package hotel_booking.controller;

import hotel_booking.dto.request.CheckInRequest;
import hotel_booking.dto.request.CheckOutRequest;
import hotel_booking.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/staff/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(@RequestBody CheckInRequest request) {
        return ResponseEntity.ok(attendanceService.checkIn(request));
    }

    @PostMapping("/check-out")
    public ResponseEntity<?> checkOut(@RequestBody CheckOutRequest request) {
        return ResponseEntity.ok(attendanceService.checkOut(request));
    }
}
