package hotel_booking.service;

import hotel_booking.dto.request.*;
import hotel_booking.dto.response.*;
import hotel_booking.entity.*;
import hotel_booking.repository.*;
import hotel_booking.specification.BookingSpecification;
import hotel_booking.util.PaginationUtil;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final RoomRepository roomRepository;
    private final RoomScheduleRepository roomScheduleRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final BookingRepository bookingRepository;
    private final RoomKeyRepository roomKeyRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final PaymentRepository paymentRepository;
    private final BookingExtendRepository bookingExtendRepository;
    private final UserRepository userRepository;
    private final CleaningTaskRepository cleaningTaskRepository;

    // 🔥 Hotel config
    private static final LocalTime CHECK_IN_TIME = LocalTime.of(12, 0);
    private static final LocalTime CHECK_OUT_TIME = LocalTime.of(11, 0);

    // ============================================
    // 1️⃣ Check availability
    // ============================================
    public CheckAvailabilityResponse checkAvailability(
            Long typeId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int quantity
    ) {
        if (typeId == null || checkInDate == null || checkOutDate == null || quantity <= 0)
            throw new RuntimeException("Invalid input");

        if (!checkInDate.isBefore(checkOutDate))
            throw new RuntimeException("Check-out must be after check-in");

        LocalDateTime checkIn = checkInDate.atTime(CHECK_IN_TIME);
        LocalDateTime checkOut = checkOutDate.atTime(CHECK_OUT_TIME);

        if (!roomTypeRepository.existsByIdAndStatus(typeId, 1))
            return buildAvailabilityResponse(0, 0);

        List<Room> rooms = roomRepository.findByTypeIdAndStatus(typeId, "AVAILABLE");
        if (rooms.isEmpty()) return buildAvailabilityResponse(0, 0);

        List<Long> roomIds = rooms.stream().map(Room::getId).toList();
        int totalRooms = roomIds.size();

        List<Long> occupiedRoomIds = roomScheduleRepository.findOccupiedRoomIds(roomIds, checkIn, checkOut);
        int availableRooms = totalRooms - occupiedRoomIds.size();
        int bookingStatus = availableRooms >= quantity ? 1 : 0;

        return buildAvailabilityResponse(bookingStatus, availableRooms);
    }

    private CheckAvailabilityResponse buildAvailabilityResponse(int status, int available) {
        CheckAvailabilityResponse res = new CheckAvailabilityResponse();
        res.setBookingStatus(status);
        res.setAvailableRooms(available);
        return res;
    }

    // ============================================
    // 2️⃣ Create booking (multiple rooms, with payment)
    // ============================================
    @Transactional
    public BookingResponse createBooking(Long userId, BookingRequest req) {
        if (userId == null) throw new RuntimeException("UNAUTHORIZED");

        if (!roomTypeRepository.existsByIdAndStatus(req.getTypeId(), 1))
            return failBooking(0);

        LocalDateTime checkIn = req.getCheckIn().atTime(CHECK_IN_TIME);
        LocalDateTime checkOut = req.getCheckOut().atTime(CHECK_OUT_TIME);

        // 🔥 Re-check availability
        List<Room> availableRooms = roomRepository.findAvailableRoomsForBooking(req.getTypeId(), checkIn, checkOut);
        if (availableRooms.size() < req.getQuantity())
            return failBooking(availableRooms.size());

        // 🔥 Select rooms
        List<Room> selectedRooms = availableRooms.subList(0, req.getQuantity());

        // 🔥 1️⃣ Tạo booking tổng
        RoomType roomType = roomTypeRepository.findById(req.getTypeId())
                .orElseThrow(() -> new RuntimeException("Room type not found"));

        long days = ChronoUnit.DAYS.between(req.getCheckIn(), req.getCheckOut());

        BigDecimal totalPrice = roomType.getPriceDay()
                .multiply(BigDecimal.valueOf(days))
                .multiply(BigDecimal.valueOf(req.getQuantity()));

        Booking booking = Booking.builder()
                .userId(userId)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .bookingType("DAY")
                .status("PENDING_PAYMENT")
                .source("DIRECT")
                .channel("WEBSITE")
                .totalPrice(totalPrice)
                .quantity(req.getQuantity())
                .createdAt(LocalDateTime.now())
                .build();

        bookingRepository.save(booking);

        // 🔥 2️⃣ Tạo RoomKey & RoomSchedule cho từng phòng
        for (Room room : selectedRooms) {
            RoomKey key = RoomKey.builder()
                    .bookingId(booking.getId())
                    .roomId(room.getId())
                    .qrCode(UUID.randomUUID().toString())
                    .numberCode(String.valueOf((int) (Math.random() * 900000 + 100000)))
                    .status(0)
                    .expiredAt(checkOut)
                    .build();
            roomKeyRepository.save(key);

            RoomSchedule schedule = RoomSchedule.builder()
                    .roomId(room.getId())
                    .bookingId(booking.getId())
                    .startTime(checkIn)
                    .endTime(checkOut)
                    .status("BOOKED")
                    .build();
            roomScheduleRepository.save(schedule);
        }

        // 🔥 3️⃣ Tạo Payment (Deposit + Final)
        BigDecimal depositAmount = totalPrice.multiply(BigDecimal.valueOf(0.3));
        BigDecimal finalAmount = totalPrice.subtract(depositAmount);
        String txnRef = String.valueOf(System.currentTimeMillis());
        Payment depositPayment = Payment.builder()
                .bookingId(booking.getId())
                .amount(depositAmount)
                .paymentType("DEPOSIT")
                .method("VNPAY")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .vnpTxnRef(txnRef)
                .build();
        paymentRepository.save(depositPayment);

        Payment finalPayment = Payment.builder()
                .bookingId(booking.getId())
                .amount(finalAmount)
                .paymentType("FINAL")
                .method("VNPAY")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .vnpTxnRef(txnRef)
                .build();
        paymentRepository.save(finalPayment);

        // 🔥 4️⃣ Notification CUSTOMER
        String message = "Booking thành công, đang giữ chỗ, vui lòng xác nhận Booking bằng việc thanh toán Deposit";
        Notification notification = Notification.builder()
                .userId(userId)
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .role("CUSTOMER")
                .build();
        notificationRepository.save(notification);

        // 🔥 5️⃣ Send email
        emailService.sendBookingConfirmation(booking, message);

        // 🔥 6️⃣ Notification RECEPTIONIST
        Notification receptionistNotification = Notification.builder()
                .userId(null)
                .message("Booking mới đã được tạo, vui lòng kiểm tra và xác nhận")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .role("RECEPTIONIST")
                .build();
        notificationRepository.save(receptionistNotification);

        return successBooking(booking.getId(), availableRooms.size());
    }

    public Page<BookingHistoryResponse> getBookingHistory(
            Long userId,
            PaginationRequest req,
            String status
    ) {

        if (userId == null) {
            throw new RuntimeException("UNAUTHORIZED");
        }

        // 🔥 default sort (mới nhất → cũ nhất)
        if (req.getSortBy() == null || req.getSortBy().isBlank()) {
            req.setSortBy("createdAt");
            req.setDirection("desc");
        }

        Pageable pageable = PaginationUtil.build(req);

        Page<Booking> result;

        if (status != null && !status.isBlank()) {
            result = bookingRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            result = bookingRepository.findByUserId(userId, pageable);
        }

        return result.map(this::toResponse);
    }

    private BookingHistoryResponse toResponse(Booking b) {
        return BookingHistoryResponse.builder()
                .id(b.getId())
                .checkIn(b.getCheckIn())
                .checkOut(b.getCheckOut())
                .status(b.getStatus())
                .bookingType(b.getBookingType())
                .totalPrice(b.getTotalPrice())
                .channel(b.getChannel())
                .createdAt(b.getCreatedAt())
                .build();
    }

    private BookingResponse failBooking(int available) {
        BookingResponse res = new BookingResponse();
        res.setBookingStatus(0);
        res.setAvailableRooms(available);
        return res;
    }

    private BookingResponse successBooking(Long bookingId, int availableBefore) {
        BookingResponse res = new BookingResponse();
        res.setBookingId(bookingId);
        res.setBookingStatus(1);
        res.setAvailableRooms(availableBefore);
        return res;
    }

    public BookingDetailResponse getBookingDetail(Long userId, Long bookingId) {

        // 🔥 check ownership
        Booking booking = bookingRepository
                .findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new RuntimeException("BOOKING_NOT_FOUND"));

        // 🔥 lấy dữ liệu liên quan
        List<RoomKey> roomKeys = roomKeyRepository.findByBookingId(bookingId);
        List<BookingExtend> extendsList = bookingExtendRepository.findByBookingId(bookingId);
        List<Payment> payments = paymentRepository.findByBookingId(bookingId);

        return BookingDetailResponse.builder()
                .booking(toBookingResponse(booking))
                .roomKeys(roomKeys.stream().map(this::toRoomKey).toList())
                .extendsList(extendsList.stream().map(this::toExtend).toList())
                .payments(payments.stream().map(this::toPayment).toList())
                .build();
    }

    public BookingDetailResponse getBookingDetail1(Long bookingId) {

        // 🔥 check ownership
        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new RuntimeException("BOOKING_NOT_FOUND"));

        // 🔥 lấy dữ liệu liên quan
        List<RoomKey> roomKeys = roomKeyRepository.findByBookingId(bookingId);
        List<BookingExtend> extendsList = bookingExtendRepository.findByBookingId(bookingId);
        List<Payment> payments = paymentRepository.findByBookingId(bookingId);

        return BookingDetailResponse.builder()
                .booking(toBookingResponse(booking))
                .roomKeys(roomKeys.stream().map(this::toRoomKey).toList())
                .extendsList(extendsList.stream().map(this::toExtend).toList())
                .payments(payments.stream().map(this::toPayment).toList())
                .build();
    }

    private BookingHistoryResponse toBookingResponse(Booking b) {
        return BookingHistoryResponse.builder()
                .id(b.getId())
                .checkIn(b.getCheckIn())
                .checkOut(b.getCheckOut())
                .status(b.getStatus())
                .bookingType(b.getBookingType())
                .totalPrice(b.getTotalPrice())
                .channel(b.getChannel())
                .createdAt(b.getCreatedAt())
                .build();
    }

    private RoomKeyResponse toRoomKey(RoomKey r) {
        return RoomKeyResponse.builder()
                .id(r.getId())
                .roomId(r.getRoomId())
                .qrCode(r.getQrCode())
                .numberCode(r.getNumberCode())
                .status(r.getStatus())
                .expiredAt(r.getExpiredAt())
                .build();
    }

    private BookingExtendResponse toExtend(BookingExtend e) {
        return BookingExtendResponse.builder()
                .id(e.getId())
                .oldCheckOut(e.getOldCheckOut())
                .newCheckOut(e.getNewCheckOut())
                .extraPrice(e.getExtraPrice())
                .build();
    }

    private PaymentResponse toPayment(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .amount(p.getAmount())
                .paymentType(p.getPaymentType())
                .method(p.getMethod())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .paidAt(p.getPaidAt())
                .build();
    }

    @Transactional
    public void cancelBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus().equals("CANCELLED")) {
            throw new RuntimeException("Booking already cancelled");
        }

        if ("CHECKED_IN".equals(booking.getStatus())) {
            throw new RuntimeException("Cannot cancel after check-in");
        }

        if ("CHECKED_OUT".equals(booking.getStatus())) {
            throw new RuntimeException("Booking already completed");
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(booking.getCheckIn())) {
            throw new RuntimeException("Cannot cancel after check-in time");
        }

        List<Payment> payments = paymentRepository.findByBookingId(bookingId);

        boolean isDepositPaid = payments.stream()
                .anyMatch(p -> p.getPaymentType().equals("DEPOSIT")
                        && p.getStatus().equals("PAID"));

        long hoursBeforeCheckin = Duration.between(
                now,
                booking.getCheckIn()
        ).toHours();

        // 🔥 CASE 3: chưa thanh toán deposit
        if (!isDepositPaid) {
            cancelAllPayments(payments);
        }

        // 🔥 CASE 1: huỷ trước >= 24h → hoàn cọc
        else if (hoursBeforeCheckin >= 24) {
            refundDeposit(payments);
        }

        // 🔥 CASE 2: huỷ trong ngày → giữ cọc
        else {
            cancelFinalOnly(payments);
        }

        // 🔥 xoá dữ liệu liên quan
        roomKeyRepository.deleteByBookingId(bookingId);
        roomScheduleRepository.deleteByBookingId(bookingId);

        // 🔥 update booking
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
    }

    private void cancelAllPayments(List<Payment> payments) {
        for (Payment p : payments) {
            p.setStatus("CANCELLED");
        }
        paymentRepository.saveAll(payments);
    }

    private void refundDeposit(List<Payment> payments) {
        for (Payment p : payments) {
            if (p.getPaymentType().equals("DEPOSIT")) {
                p.setStatus("REFUNDED");

                // 👉 nếu có tích hợp PayOS / VNPAY thì gọi refund ở đây
                // paymentGateway.refund(p);

            } else if (p.getPaymentType().equals("FINAL")) {
                p.setStatus("CANCELLED");
            }
        }
        paymentRepository.saveAll(payments);
    }

    private void cancelFinalOnly(List<Payment> payments) {
        for (Payment p : payments) {
            if (p.getPaymentType().equals("FINAL")) {
                p.setStatus("CANCELLED");
            }
        }
        paymentRepository.saveAll(payments);
    }

    @Transactional
    public void extendBooking(BookingExtendRequest request) {

        // 1️⃣ Lấy booking
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 2️⃣ Kiểm tra điều kiện extend
        LocalDateTime now = LocalDateTime.now();
        if (!"CHECKED_IN".equals(booking.getStatus())) {
            throw new RuntimeException("Booking not checked in, cannot extend");
        }

        if (now.isAfter(booking.getCheckOut())) {
            throw new RuntimeException("Booking already past check-out, cannot extend");
        }

        // 3️⃣ Lấy danh sách phòng trong booking
        List<RoomKey> roomKeys = roomKeyRepository.findByBookingId(booking.getId());
        if (roomKeys.isEmpty()) {
            throw new RuntimeException("No rooms in booking");
        }

        LocalDateTime oldCheckOut = booking.getCheckOut();
        LocalDateTime newCheckOut = oldCheckOut.plusDays(request.getExtraDays());

        // 4️⃣ Kiểm tra xung đột lịch cho từng phòng
        for (RoomKey rk : roomKeys) {
            boolean conflict = roomScheduleRepository.existsByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(
                    rk.getRoomId(), newCheckOut, oldCheckOut
            );
            if (conflict) {
                throw new RuntimeException("Room " + rk.getRoomId() + " is not available for extension");
            }
        }

        // 5️⃣ Update RoomSchedules: mở rộng thời gian end_time
        for (RoomKey rk : roomKeys) {
            RoomSchedule schedule = roomScheduleRepository.findLastScheduleByRoomId(rk.getRoomId(), booking.getId())
                    .orElseThrow(() -> new RuntimeException("Schedule not found for room " + rk.getRoomId()));
            schedule.setEndTime(newCheckOut);
            roomScheduleRepository.save(schedule);
        }

        // 6️⃣ Update RoomKey.expired_at
        for (RoomKey rk : roomKeys) {
            rk.setExpiredAt(newCheckOut);
            roomKeyRepository.save(rk);
        }

        // 7️⃣ Tính tiền gia hạn = số phòng × giá thuê 1 ngày × số ngày
        BigDecimal extraPrice = BigDecimal.ZERO;
        for (RoomKey rk : roomKeys) {
            Room room = roomRepository.findById(rk.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Room not found"));
            RoomType roomType = roomTypeRepository.findById(room.getTypeId()).orElseThrow(() -> new RuntimeException("Room Type not found"));
            BigDecimal pricePerDay = roomType.getPriceDay();
            extraPrice = extraPrice.add(pricePerDay);
        }
        extraPrice = extraPrice.multiply(BigDecimal.valueOf(request.getExtraDays()));

        // 8️⃣ Tạo record BookingExtend
        BookingExtend extend = new BookingExtend();
        extend.setBookingId(booking.getId());
        extend.setOldCheckOut(oldCheckOut);
        extend.setNewCheckOut(newCheckOut);
        extend.setExtraPrice(extraPrice);
        bookingExtendRepository.save(extend);

        // 9️⃣ Update tổng tiền booking
        booking.setCheckOut(newCheckOut);
        booking.setTotalPrice(booking.getTotalPrice().add(extraPrice));
        bookingRepository.save(booking);

        // 🔟 Tạo payment EXTEND
        Payment payment = new Payment();
        payment.setBookingId(booking.getId());
        payment.setAmount(extraPrice);
        payment.setPaymentType("EXTEND");
        payment.setStatus("PENDING"); // Khách thanh toán sau
        payment.setMethod("VNPAY");
        payment.setCreatedAt(LocalDateTime.now());
        payment.setVnpTxnRef(String.valueOf(System.currentTimeMillis()));
        paymentRepository.save(payment);
    }

    @Transactional
    public BookingResponse createOtaBooking(OtaBookingRequest otaReq) {
        if (otaReq.getRoomTypeId() == null || otaReq.getCheckIn() == null || otaReq.getCheckOut() == null) {
            throw new RuntimeException("Invalid OTA booking input");
        }

        LocalDateTime checkIn = LocalDateTime.parse(otaReq.getCheckIn());
        LocalDateTime checkOut = LocalDateTime.parse(otaReq.getCheckOut());

        if (!checkIn.isBefore(checkOut)) {
            throw new RuntimeException("Check-out must be after check-in");
        }

        RoomType roomType = roomTypeRepository.findById(otaReq.getRoomTypeId())
                .orElseThrow(() -> new RuntimeException("Room type not found"));
        if (roomType.getStatus() != 1) {
            throw new RuntimeException("Room type inactive");
        }

        List<Room> roomsOfType = roomRepository.findByTypeIdAndStatus(roomType.getId(), "AVAILABLE");
        if (roomsOfType.isEmpty()) {
            throw new RuntimeException("No available rooms for this type");
        }

        List<Long> roomIds = roomsOfType.stream().map(Room::getId).toList();
        List<Long> occupiedRoomIds = roomScheduleRepository.findOccupiedRoomIds(roomIds, checkIn, checkOut);
        List<Room> availableRooms = roomsOfType.stream()
                .filter(r -> !occupiedRoomIds.contains(r.getId()))
                .toList();

        int requiredQuantity = otaReq.getRooms().stream().mapToInt(r -> r.getQuantity()).sum();

        if (availableRooms.size() < requiredQuantity) {
            throw new IllegalArgumentException("Not enough available rooms. Requested: "
                    + requiredQuantity + ", Available: " + availableRooms.size());
        }

        // 🔥 Chọn phòng
        List<Room> selectedRooms = availableRooms.subList(0, requiredQuantity);

        // ... Phần còn lại giữ nguyên như cũ
        Booking booking = Booking.builder()
                .userId(null)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .bookingType(otaReq.getBookingType())
                .status("PENDING_PAYMENT")
                .source("OTA")
                .channel(otaReq.getOtaName())
                .totalPrice(otaReq.getTotalPrice())
                .quantity(requiredQuantity)
                .note(otaReq.getNote() + " | Guest: " + otaReq.getGuestName() + ", " + otaReq.getGuestCount())
                .createdAt(LocalDateTime.now())
                .build();
        bookingRepository.save(booking);

        // 🔹 6️⃣ Tạo RoomKey + RoomSchedule cho từng phòng
        for (Room room : selectedRooms) {
            RoomKey key = RoomKey.builder()
                    .bookingId(booking.getId())
                    .roomId(room.getId())
                    .qrCode(UUID.randomUUID().toString())
                    .numberCode(String.valueOf((int) (Math.random() * 900000 + 100000)))
                    .status(0)
                    .expiredAt(checkOut)
                    .build();
            roomKeyRepository.save(key);

            RoomSchedule schedule = RoomSchedule.builder()
                    .roomId(room.getId())
                    .bookingId(booking.getId())
                    .startTime(checkIn)
                    .endTime(checkOut)
                    .status("BOOKED")
                    .build();
            roomScheduleRepository.save(schedule);
        }

        // 🔹 7️⃣ Tạo Payment record
        Payment payment = Payment.builder()
                .bookingId(booking.getId())
                .amount(otaReq.getTotalPrice())
                .paymentType("FINAL")
                .method(otaReq.getPaymentMethod())
                .status(otaReq.getPaymentStatus())
                .transactionCode(otaReq.getTransactionCode())
                .createdAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        // 🔹 8️⃣ Notification RECEPTIONIST
        Notification notification = Notification.builder()
                .userId(null)
                .message("New OTA booking (" + otaReq.getOtaName() + ") created, please check system.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .role("RECEPTIONIST")
                .build();
        notificationRepository.save(notification);

        return successBooking(booking.getId(), availableRooms.size());
    }

    public BookingListResponse getBookings(
            int page,
            int size,
            String status,
            String source,
            String channel,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Specification<Booking> spec = BookingSpecification.filter(
                status,
                source,
                channel,
                fromDate != null ? fromDate.atStartOfDay() : null,
                toDate != null ? toDate.atTime(23, 59, 59) : null
        );

        Page<Booking> bookingPage = bookingRepository.findAll(spec, pageable);

        List<BookingDTO> list = bookingPage.getContent().stream().map(this::toDTO).toList();

        return new BookingListResponse(
                list,
                bookingPage.getTotalElements(),
                page,
                size
        );
    }

    private BookingDTO toDTO(Booking b) {
        BookingDTO dto = new BookingDTO();
        dto.setId(b.getId().intValue());
        if (b.getUserId() != null) {
            dto.setUserId(b.getUserId().intValue());
        } else {
            dto.setUserId(null); // hoặc 0 nếu bạn muốn
        }
        dto.setCheckIn(b.getCheckIn());
        dto.setCheckOut(b.getCheckOut());
        dto.setBookingType(b.getBookingType());
        dto.setStatus(b.getStatus());
        dto.setSource(b.getSource());
        dto.setChannel(b.getChannel());
        dto.setTotalPrice(b.getTotalPrice());
        dto.setCreatedAt(b.getCreatedAt());
        dto.setNote(b.getNote());
        dto.setQuantity(b.getQuantity());
        return dto;
    }

    public BookingListResponse searchBookings(String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Specification<Booking> spec =
                BookingSpecification.search(keyword);

        Page<Booking> result = bookingRepository.findAll(spec, pageable);

        List<BookingDTO> list = result.getContent()
                .stream()
                .map(this::toDTO)
                .toList();

        return new BookingListResponse(
                list,
                result.getTotalElements(),
                page,
                size
        );
    }

    public BookingDetailResponse getBookingDetail(Long bookingId) {

        // 🔥 check ownership
        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new RuntimeException("BOOKING_NOT_FOUND"));

        // 🔥 lấy dữ liệu liên quan
        List<RoomKey> roomKeys = roomKeyRepository.findByBookingId(bookingId);
        List<BookingExtend> extendsList = bookingExtendRepository.findByBookingId(bookingId);
        List<Payment> payments = paymentRepository.findByBookingId(bookingId);

        return BookingDetailResponse.builder()
                .booking(toBookingResponse(booking))
                .roomKeys(roomKeys.stream().map(this::toRoomKey).toList())
                .extendsList(extendsList.stream().map(this::toExtend).toList())
                .payments(payments.stream().map(this::toPayment).toList())
                .build();
    }

    @Transactional
    public void checkIn(Long bookingId) {

        // ===== STEP 1: LẤY BOOKING =====
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // ===== STEP 2: VALIDATE =====

        // 2.1 Status phải là BOOKED
        if (!"BOOKED".equalsIgnoreCase(booking.getStatus())) {
            throw new RuntimeException("Booking must be BOOKED to check-in");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkIn = booking.getCheckIn();
        LocalDateTime checkOut = booking.getCheckOut();

        // 2.2 Chưa tới giờ
        if (now.isBefore(checkIn)) {
            throw new RuntimeException("Too early to check-in");
        }

        // 2.3 Quá giờ checkout
        if (!now.isBefore(checkOut)) {
            throw new RuntimeException("Booking expired");
        }

        // ===== STEP 3: UPDATE BOOKING =====
        booking.setStatus("CHECKED_IN");
        bookingRepository.save(booking);

        // ===== STEP 4: UPDATE ROOM SCHEDULE =====
        List<RoomSchedule> schedules =
                roomScheduleRepository.findByBookingId(bookingId);

        if (schedules.isEmpty()) {
            throw new RuntimeException("No RoomSchedules found");
        }

        for (RoomSchedule rs : schedules) {
            rs.setStatus("OCCUPIED");
        }
        roomScheduleRepository.saveAll(schedules);

        // ===== STEP 5: UPDATE ROOM KEY =====
        List<RoomKey> keys = roomKeyRepository.findByBookingId(bookingId);

        if (keys.isEmpty()) {
            throw new RuntimeException("No RoomKeys found");
        }

        for (RoomKey key : keys) {
            key.setStatus(1); // ACTIVE
        }
        roomKeyRepository.saveAll(keys);

        // ===== DONE =====
    }


    // ============================================
    // 2️⃣ Create booking (multiple rooms, with payment)
    // ============================================
    @Transactional
    public BookingResponse createBooking( BookingReceptionistRequest req) {

        if (!roomTypeRepository.existsByIdAndStatus(req.getTypeId(), 1))
            return failBooking(0);

        LocalDateTime checkIn = req.getCheckIn().atTime(CHECK_IN_TIME);
        LocalDateTime checkOut = req.getCheckOut().atTime(CHECK_OUT_TIME);

        // 🔥 Re-check availability
        List<Room> availableRooms = roomRepository.findAvailableRoomsForBooking(req.getTypeId(), checkIn, checkOut);
        if (availableRooms.size() < req.getQuantity())
            return failBooking(availableRooms.size());

        // 🔥 Select rooms
        List<Room> selectedRooms = availableRooms.subList(0, req.getQuantity());

        // 🔥 1️⃣ Tạo booking tổng
        RoomType roomType = roomTypeRepository.findById(req.getTypeId())
                .orElseThrow(() -> new RuntimeException("Room type not found"));

        long days = ChronoUnit.DAYS.between(req.getCheckIn(), req.getCheckOut());

        BigDecimal totalPrice = roomType.getPriceDay()
                .multiply(BigDecimal.valueOf(days))
                .multiply(BigDecimal.valueOf(req.getQuantity()));

        Booking booking = Booking.builder()
                .userId(null)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .bookingType("DAY")
                .status("PENDING_PAYMENT")
                .source("DIRECT")
                .channel("WALKIN")
                .totalPrice(totalPrice)
                .quantity(req.getQuantity())
                .createdAt(LocalDateTime.now())
                .note(req.getFullName() + " - " + req.getPhone() + " - " + req.getAddress() )
                .build();

        bookingRepository.save(booking);

        // 🔥 2️⃣ Tạo RoomKey & RoomSchedule cho từng phòng
        for (Room room : selectedRooms) {
            RoomKey key = RoomKey.builder()
                    .bookingId(booking.getId())
                    .roomId(room.getId())
                    .qrCode(UUID.randomUUID().toString())
                    .numberCode(String.valueOf((int) (Math.random() * 900000 + 100000)))
                    .status(0)
                    .expiredAt(checkOut)
                    .build();
            roomKeyRepository.save(key);

            RoomSchedule schedule = RoomSchedule.builder()
                    .roomId(room.getId())
                    .bookingId(booking.getId())
                    .startTime(checkIn)
                    .endTime(checkOut)
                    .status("BOOKED")
                    .build();
            roomScheduleRepository.save(schedule);
        }

        // 🔥 3️⃣ Tạo Payment (Deposit + Final)
        BigDecimal depositAmount = totalPrice.multiply(BigDecimal.valueOf(0.3));
        BigDecimal finalAmount = totalPrice.subtract(depositAmount);
        String txnRef = String.valueOf(System.currentTimeMillis());
        Payment depositPayment = Payment.builder()
                .bookingId(booking.getId())
                .amount(depositAmount)
                .paymentType("DEPOSIT")
                .method("VNPAY")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .vnpTxnRef(txnRef)
                .build();
        paymentRepository.save(depositPayment);

        Payment finalPayment = Payment.builder()
                .bookingId(booking.getId())
                .amount(finalAmount)
                .paymentType("FINAL")
                .method("VNPAY")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .vnpTxnRef(txnRef)
                .build();
        paymentRepository.save(finalPayment);

        // 🔥 6️⃣ Notification RECEPTIONIST
        Notification receptionistNotification = Notification.builder()
                .userId(null)
                .message("Booking mới đã được tạo, vui lòng kiểm tra và xác nhận")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .role("RECEPTIONIST")
                .build();
        notificationRepository.save(receptionistNotification);

        return successBooking(booking.getId(), availableRooms.size());
    }

    @Transactional
    public void checkOut(Long bookingId) {

        // 🔥 1. Lấy booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!"CHECKED_IN".equals(booking.getStatus())) {
            throw new RuntimeException("Booking is not checked-in");
        }

        // 🔥 2. Update booking -> CHECK_PENALTY
        booking.setStatus("CHECK_PENALTY");
        bookingRepository.save(booking);

        // 🔥 3. Lấy danh sách RoomKey
        List<RoomKey> roomKeys = roomKeyRepository.findByBookingId(bookingId);

        if (roomKeys.isEmpty()) {
            throw new RuntimeException("No room keys found");
        }

        // 🔥 4. Lấy CLEANER đang đi làm hôm nay
        List<User> cleaners = userRepository.findAvailableCleanersToday();

        if (cleaners.isEmpty()) {
            throw new RuntimeException("No cleaner available");
        }

        int index = 0;

        // 🔥 5. Loop từng phòng
        for (RoomKey key : roomKeys) {

            // 👉 5.1 Disable key
            key.setStatus(0);
            roomKeyRepository.save(key);

            Long roomId = key.getRoomId();

            // 👉 5.2 Chọn cleaner (round-robin)
            User cleaner = cleaners.get(index % cleaners.size());
            index++;

            // 👉 5.3 Tạo cleaning task
            CleaningTask task = CleaningTask.builder()
                    .roomId(roomId)
                    .bookingId(bookingId)
                    .cleanerId(cleaner.getId().longValue())
                    .status("DOING")
                    .createdAt(LocalDateTime.now())
                    .build();

            cleaningTaskRepository.save(task);

            // 👉 5.4 Update room -> CLEANING
            Room room = roomRepository.findById(roomId).orElseThrow();
            room.setStatus("CLEANING");
            roomRepository.save(room);
        }
    }


    public Page<Booking> getBookings(
            String status,
            LocalDate date,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("created_at").descending()
        );

        return bookingRepository.searchBookings(status, date, pageable);
    }


    public Page<Booking> search(BookingSearchRequest request, int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt") // 🔥 mới nhất
        );

        Specification<Booking> spec = BookingSpecification.filter(request);

        return bookingRepository.findAll(spec, pageable);
    }

    public Page<Booking> getOtaBookings(String channel, int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Specification<Booking> spec = (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // 🔥 luôn là OTA
            predicates.add(cb.equal(root.get("source"), "OTA"));

            // 🔥 filter theo channel nếu có
            if (channel != null && !channel.isEmpty()) {
                predicates.add(cb.equal(root.get("channel"), channel));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return bookingRepository.findAll(spec, pageable);
    }

}