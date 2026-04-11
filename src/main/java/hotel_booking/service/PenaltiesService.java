package hotel_booking.service;

import hotel_booking.dto.request.PaginationRequest;
import hotel_booking.dto.request.PenaltyCreateRequest;
import hotel_booking.dto.request.PenaltyUpdateRequest;
import hotel_booking.dto.response.PenaltyResponse;
import hotel_booking.entity.Penalty;
import hotel_booking.entity.User;
import hotel_booking.repository.PenaltyRepository;
import hotel_booking.repository.UserRepository;
import hotel_booking.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PenaltiesService {

    private final PenaltyRepository penaltyRepository;
    private final UserRepository userRepository;

    public void createPenalty(PenaltyCreateRequest req) {

        // 🔥 validate user
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔥 validate amount
        if (req.getAmount() == null || req.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be > 0");
        }

        Penalty p = new Penalty();
        p.setUserId(user.getId().longValue());
        p.setWorkDate(req.getWorkDate());
        p.setAmount(req.getAmount());
        p.setReason(req.getReason());
        p.setCreatedAt(LocalDateTime.now());

        penaltyRepository.save(p);
    }

    public Page<PenaltyResponse> getPenaltiesByDate(
            PaginationRequest req,
            LocalDate workDate,
            Long userId // optional
    ) {

        if (workDate == null) {
            throw new RuntimeException("workDate is required");
        }

        // 🔥 default sort: mới nhất trước
        if (req.getSortBy() == null || req.getSortBy().isBlank()) {
            req.setSortBy("createdAt");
            req.setDirection("desc");
        }

        Pageable pageable = PaginationUtil.build(req);

        Page<Penalty> result;

        if (userId != null) {
            result = penaltyRepository
                    .findByWorkDateAndUserId(workDate, userId, pageable);
        } else {
            result = penaltyRepository
                    .findByWorkDate(workDate, pageable);
        }

        return result.map(this::toResponse);
    }

    public PenaltyResponse getPenaltyById(Long id) {

        Penalty p = penaltyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Penalty not found"));

        return toResponse(p);
    }

    public void updatePenalty(Long id, PenaltyUpdateRequest req) {

        Penalty p = penaltyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Penalty not found"));

        LocalDate workDate = p.getWorkDate();

        // 🔥 deadline = 7h sáng ngày hôm sau
        LocalDateTime deadline = workDate
                .plusDays(1)
                .atTime(7, 0);

        LocalDateTime now = LocalDateTime.now();

        // ❌ quá hạn
        if (now.isAfter(deadline)) {
            throw new RuntimeException("Cannot update penalty after 7AM next day");
        }

        // ✅ update fields (chỉ update nếu có truyền)
        if (req.getWorkDate() != null) {
            p.setWorkDate(req.getWorkDate());
        }

        if (req.getAmount() != null) {
            if (req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Amount must be > 0");
            }
            p.setAmount(req.getAmount());
        }

        if (req.getReason() != null) {
            p.setReason(req.getReason());
        }

        penaltyRepository.save(p);
    }

    public void deletePenalty(Long id) {

        Penalty p = penaltyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Penalty not found"));

        LocalDate workDate = p.getWorkDate();

        // 🔥 deadline = 7h sáng ngày hôm sau
        LocalDateTime deadline = workDate
                .plusDays(1)
                .atTime(7, 0);

        LocalDateTime now = LocalDateTime.now();

        // ❌ quá hạn thì không cho xoá
        if (now.isAfter(deadline)) {
            throw new RuntimeException("Cannot delete penalty after 7AM next day");
        }

        penaltyRepository.delete(p);
    }

    private PenaltyResponse toResponse(Penalty p) {
        PenaltyResponse res = new PenaltyResponse();

        res.setId(p.getId());
        res.setUserId(p.getUserId());
        User user = userRepository.findById(p.getUserId()).orElseThrow(() -> new RuntimeException("You have not checked in yet"));
        res.setUsername(user.getUsername());
        res.setWorkDate(p.getWorkDate());
        res.setAmount(p.getAmount());
        res.setReason(p.getReason());
        res.setCreatedAt(p.getCreatedAt());

        return res;
    }
}
