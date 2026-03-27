package hotel_booking.service;

import hotel_booking.dto.request.ReviewRequest;
import hotel_booking.dto.response.RoomBookingResponse;
import hotel_booking.entity.Booking;
import hotel_booking.entity.Review;
import hotel_booking.entity.RoomKey;
import hotel_booking.repository.BookingRepository;
import hotel_booking.repository.ReviewRepository;
import hotel_booking.repository.RoomKeyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final RoomKeyRepository roomKeyRepository;

    @Transactional
    public void createReview(Long userId, ReviewRequest req) {

        Booking booking = bookingRepository.findById(req.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 🔹 1. check owner
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("You do not own this booking");
        }

        // 🔹 2. check status FINISHED
        if (!"FINISHED".equals(booking.getStatus())) {
            throw new RuntimeException("Booking not finished yet");
        }

        // 🔹 3. check trong vòng 3 ngày
        LocalDateTime checkOut = booking.getCheckOut();
        LocalDateTime deadline = checkOut.plusDays(3);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new RuntimeException("Review time expired (3 days)");
        }

        // 🔹 4. check phòng có trong booking
        boolean roomExists = roomKeyRepository.findByBookingId(req.getBookingId()).stream()
                .anyMatch(rk -> rk.getRoomId().equals(req.getRoomId()));

        if (!roomExists) {
            throw new RuntimeException("This room is not part of your booking");
        }

        // 🔹 5. check đã review phòng này chưa
        if (reviewRepository.existsByBookingIdAndRoomId(req.getBookingId(), req.getRoomId())) {
            throw new RuntimeException("You already reviewed this room in this booking");
        }

        // 🔹 6. validate rating
        if (req.getRating() < 1 || req.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        // 🔹 7. tạo review
        Review review = new Review();
        review.setBookingId(req.getBookingId());
        review.setRoomId(req.getRoomId());
        review.setUserId(userId);
        review.setRating(req.getRating());
        review.setComment(req.getComment());

        reviewRepository.save(review);
    }

    public List<RoomBookingResponse> getRoomsForBooking(Long bookingId, Long userId) {

        // 🔹 1. Lấy booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 🔹 2. Check owner
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("You do not own this booking");
        }

        // 🔹 3. Chỉ hiển thị khi status = FINISHED
        if (!"FINISHED".equals(booking.getStatus())) {
            // có thể return rỗng hoặc ném lỗi
            return Collections.emptyList();
            // hoặc ném lỗi:
            // throw new RuntimeException("Booking not finished yet");
        }

        // 🔹 4. Lấy tất cả phòng liên quan booking
        List<RoomKey> roomKeys = roomKeyRepository.findByBookingId(bookingId);

        // 🔹 5. Chuyển sang DTO
        return roomKeys.stream()
                .map(rk -> new RoomBookingResponse(
                        rk.getBookingId(),
                        rk.getRoomId(),
                        rk.getNumberCode()
                ))
                .collect(Collectors.toList());
    }
}
