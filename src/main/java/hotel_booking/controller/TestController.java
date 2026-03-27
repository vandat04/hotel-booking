package hotel_booking.controller;

import hotel_booking.dto.request.OtaBookingRequest;
import hotel_booking.dto.response.BookingResponse;
import hotel_booking.service.AuthService;
import hotel_booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class TestController {
    private final AuthService authService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String admin() {
        return "Hello ADMIN";
    }

    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String customer() {
        return "Hello CUSTOMER";
    }
    private final BookingService bookingService;
    @PostMapping("/ota")
    public ResponseEntity<BookingResponse> createOtaBooking(@RequestBody OtaBookingRequest otaReq) {
        try {
            BookingResponse response = bookingService.createOtaBooking(otaReq);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            // Trả về lỗi với status 400 và message
            BookingResponse booking = new BookingResponse();
            booking.setBookingId(null);
            booking.setBookingId(null);
            booking.setBookingStatus(0);
            booking.setAvailableRooms(0);
            return ResponseEntity
                    .badRequest()
                    .body(booking); // BookingId=null, bookingStatus=0, availableRooms=0
        }
    }
}
