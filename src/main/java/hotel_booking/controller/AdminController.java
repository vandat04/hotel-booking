package hotel_booking.controller;

import hotel_booking.dto.request.BookingSearchRequest;
import hotel_booking.dto.response.BookingDetailResponse;
import hotel_booking.dto.response.UserProfileResponse;
import hotel_booking.entity.Booking;
import hotel_booking.security.CustomUserDetails;
import hotel_booking.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DashboardService dashboardService;
    private final AttendanceService attendanceService;
    private final CleanTaskService cleanTaskService;
    private final PaymentService paymentService;
    private final BookingService bookingService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/attendance")
    public ResponseEntity<?> getAttendance(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Integer slotId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                attendanceService.getAttendance(date, slotId, page, size)
        );
    }

    @GetMapping("/cleaning-tasks")
    public ResponseEntity<?> getCleaningTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        LocalDate parsedDate = null;

        if (date != null) {
            parsedDate = LocalDate.parse(date);
        }

        return ResponseEntity.ok(
                cleanTaskService.getCleaningTasks(status, parsedDate, page, size)
        );
    }

    @GetMapping("/payments")
    public ResponseEntity<?> getPayments(
            @RequestParam(required = false) Integer bookingId,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        LocalDate parsedDate = null;

        if (date != null) {
            parsedDate = LocalDate.parse(date);
        }

        return ResponseEntity.ok(
                paymentService.getPayments(
                        bookingId, method, status, parsedDate, page, size
                )
        );
    }

    @GetMapping("/payments/statistics")
    public ResponseEntity<?> getStatisticByType(
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date
    ) {

        LocalDate parsedDate = null;

        if (date != null) {
            parsedDate = LocalDate.parse(date);
        }

        return ResponseEntity.ok(
                paymentService.getStatisticByType(method, status, parsedDate)
        );
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> getBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        LocalDate parsedDate = null;

        if (date != null) {
            parsedDate = LocalDate.parse(date);
        }

        return ResponseEntity.ok(
                bookingService.getBookings(status, parsedDate, page, size)
        );
    }

    @GetMapping("/bookings/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        BookingSearchRequest request = new BookingSearchRequest();
        request.setUserId(userId);
        request.setRoomNumber(roomNumber);
        request.setChannel(channel);
        request.setDate(date);

        Page<Booking> result = bookingService.search(request, page, size);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/bookings/{id}")
    public BookingDetailResponse getDetail(
            @PathVariable Long id
    ) {
        return bookingService.getBookingDetail1(id);
    }

    @GetMapping("/bookings/ota")
    public ResponseEntity<?> getOtaBookings(
            @RequestParam(required = false) String channel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(bookingService.getOtaBookings(channel, page, size));
    }

}
