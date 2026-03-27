package hotel_booking.controller;

import hotel_booking.dto.request.PaginationRequest;
import hotel_booking.dto.request.PenaltyCreateRequest;
import hotel_booking.dto.request.PenaltyUpdateRequest;
import hotel_booking.repository.PenaltyRepository;
import hotel_booking.service.PenaltiesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/penalties")
@RequiredArgsConstructor
public class PenaltiesController {

    private final PenaltiesService penaltiesService;

    @PostMapping
    public ResponseEntity<?> createPenalty(@RequestBody PenaltyCreateRequest req) {

        penaltiesService.createPenalty(req);

        return ResponseEntity.ok("Penalty created successfully");
    }

    @GetMapping
    public ResponseEntity<?> getPenaltiesByDate(
            @ModelAttribute PaginationRequest req,
            @RequestParam LocalDate workDate,
            @RequestParam(required = false) Long userId
    ) {

        return ResponseEntity.ok(
                penaltiesService.getPenaltiesByDate(req, workDate, userId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPenaltyDetail(@PathVariable Long id) {

        return ResponseEntity.ok(
                penaltiesService.getPenaltyById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePenalty(
            @PathVariable Long id,
            @RequestBody PenaltyUpdateRequest req
    ) {

        penaltiesService.updatePenalty(id, req);

        return ResponseEntity.ok("Penalty updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePenalty(@PathVariable Long id) {

        penaltiesService.deletePenalty(id);

        return ResponseEntity.ok("Penalty deleted successfully");
    }
}