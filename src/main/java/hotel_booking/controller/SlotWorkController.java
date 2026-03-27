package hotel_booking.controller;

import hotel_booking.dto.request.CreateSlotWorkRequest;
import hotel_booking.dto.request.UpdateSlotWorkRequest;
import hotel_booking.service.SlotWorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/slot-work")
@RequiredArgsConstructor
public class SlotWorkController {

    private final SlotWorkService slotWorkService;

    // CREATE
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateSlotWorkRequest request) {
        return ResponseEntity.ok(slotWorkService.create(request));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(slotWorkService.getAll());
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(slotWorkService.getById(id));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody UpdateSlotWorkRequest request
    ) {
        return ResponseEntity.ok(slotWorkService.update(id, request));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        slotWorkService.delete(id);
        return ResponseEntity.ok("Deleted successfully");
    }
}