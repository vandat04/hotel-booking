package hotel_booking.controller;

import hotel_booking.dto.request.BookingReceptionistRequest;
import hotel_booking.dto.request.PayRequest;
import hotel_booking.dto.response.*;
import hotel_booking.service.BookingService;
import hotel_booking.service.DashboardService;
import hotel_booking.service.PaymentService;
import hotel_booking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/receptionist")
@RequiredArgsConstructor
public class ReceptionistController
{

    private final BookingService bookingService;
    private final UserService userService;
    private  final DashboardService dashboardService;
    private final PaymentService paymentService;


    @GetMapping("/dashboard")
    public List<RoomTypeDashboardDTO> getDashboard(
            @RequestParam(required = false) Long typeIds
    ) {
        return Collections.singletonList(dashboardService.getReceptionistDashboard(typeIds));
    }

    @GetMapping("/bookings")
    public ResponseEntity<BookingListResponse> getBookings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {

        BookingListResponse response = bookingService.getBookings(
                page, size, status, source, channel, fromDate, toDate
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/bookings/search")
    public ResponseEntity<BookingListResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                bookingService.searchBookings(keyword, page, size)
        );
    }

    @GetMapping("/bookings/{id}")
    public BookingDetailResponse getDetail(
            @PathVariable Long id
    ) {
        return bookingService.getBookingDetail(id);
    }

    @PutMapping("/bookings/{id}/check-in")
    public ResponseEntity<?> checkIn(@PathVariable Long id) {
        bookingService.checkIn(id);
        return ResponseEntity.ok("Check-in successful");
    }

    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok("Booking cancelled successfully");
    }

    @GetMapping("/bookings/check-availability")
    public CheckAvailabilityResponse checkAvailability(
            @RequestParam Long typeId,
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam int quantity
    ) {
        return bookingService.checkAvailability(
                typeId,
                LocalDate.parse(checkIn),
                LocalDate.parse(checkOut),
                quantity);
    }

    @PostMapping("/bookings/create")
    public BookingResponse book(@RequestBody BookingReceptionistRequest request) {

        return bookingService.createBooking(request);
    }


    @PostMapping("/bookings/{id}/check-out")
    public ResponseEntity<?> checkOut(@PathVariable Long id) {
        bookingService.checkOut(id);
        return ResponseEntity.ok("Check-out success");
    }

    @GetMapping("/bookings/{id}/check-payment")
    public ResponseEntity<?> getPaymentSummary(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.calculatePayment(id));
    }

    @PostMapping("/bookings/{id}/pay-single")
    public ResponseEntity<?> paySingle(@PathVariable Long id, @RequestBody PayRequest request) {
        request.setBookingId(id);
        return ResponseEntity.ok(paymentService.paySinglePayment(request));
    }

    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenue(
            @RequestParam(required = false) Long typeId
    ) {
        return ResponseEntity.ok(paymentService.getTodayRevenue(typeId));
    }

}
