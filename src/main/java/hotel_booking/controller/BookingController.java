package hotel_booking.controller;

import hotel_booking.dto.request.BookingExtendRequest;
import hotel_booking.dto.request.BookingRequest;
import hotel_booking.dto.request.PaginationRequest;
import hotel_booking.dto.request.ReviewRequest;
import hotel_booking.dto.response.*;
import hotel_booking.security.CustomUserDetails;
import hotel_booking.service.BookingService;
import hotel_booking.service.ReviewService;
import hotel_booking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hotel")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final ReviewService reviewService;

    @GetMapping("/room-types/{typeId}/check-availability")
    public CheckAvailabilityResponse checkAvailability(
            @PathVariable Long typeId,
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam int quantity
    ) {
        return bookingService.checkAvailability(
                typeId,
                LocalDate.parse(checkIn),
                LocalDate.parse(checkOut),
                quantity
        );
    }

    @PostMapping("/room-types/{typeId}/bookings")
    public BookingResponse book(@RequestBody BookingRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 🔥 lấy userId từ principal (tuỳ bạn set trong JWT)
        Long userId = Long.parseLong(auth.getName());

        UserProfileResponse user = userService.getMyProfile();

        return bookingService.createBooking(user.getId().longValue(), request);
    }

    @GetMapping("/bookings-history")
    public Page<BookingHistoryResponse> getHistory(
            @ModelAttribute PaginationRequest req,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 🔥 lấy userId từ principal (tuỳ bạn set trong JWT)
        Long userId = Long.parseLong(auth.getName());

        UserProfileResponse user1 = userService.getMyProfile();
        return bookingService.getBookingHistory(user1.getId().longValue(), req, status);
    }

    @GetMapping("/bookings-history/{id}")
    public BookingDetailResponse getDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 🔥 lấy userId từ principal (tuỳ bạn set trong JWT)
        Long userId = Long.parseLong(auth.getName());

        UserProfileResponse user2 = userService.getMyProfile();
        return bookingService.getBookingDetail(user2.getId().longValue(), id);
    }

    @PostMapping("/bookings-history/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok("Booking cancelled successfully");
    }

    @PostMapping("/bookings-history/{id}/rooms-for-review/review")
    public ResponseEntity<?> createReview(
            @RequestBody ReviewRequest request
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 🔥 lấy userId từ principal (tuỳ bạn set trong JWT)
        Long userId2 = Long.parseLong(auth.getName());

        reviewService.createReview(userId2, request);
        return ResponseEntity.ok("Review created");
    }

    @GetMapping("/bookings-history/{id}/rooms-for-review")
    public ResponseEntity<List<RoomBookingResponse>> getRoomsForReview(
            @PathVariable Long id
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 🔥 lấy userId từ principal (tuỳ bạn set trong JWT)
        Long userId1 = Long.parseLong(auth.getName());

        List<RoomBookingResponse> rooms = reviewService.getRoomsForBooking(id, userId1);
        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/bookings-history/{id}/extend")
    public ResponseEntity<?> extendBooking(@PathVariable Long id, @RequestParam  int extraDays) {
        try {
            // Gọi service thực hiện extend
            BookingExtendRequest request = new BookingExtendRequest();
            request.setBookingId(id);
            request.setExtraDays(extraDays);
            bookingService.extendBooking(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Booking extended successfully"
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", ex.getMessage()
            ));
        }

    }

}
