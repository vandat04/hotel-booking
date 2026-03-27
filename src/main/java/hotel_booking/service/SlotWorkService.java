package hotel_booking.service;

import hotel_booking.dto.request.CreateSlotWorkRequest;
import hotel_booking.dto.request.UpdateSlotWorkRequest;
import hotel_booking.dto.response.SlotWorkResponse;
import hotel_booking.entity.SlotWork;
import hotel_booking.repository.SlotWorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SlotWorkService {

    private final SlotWorkRepository slotWorkRepository;

    // ================= CREATE =================
    public SlotWorkResponse create(CreateSlotWorkRequest request) {

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        SlotWork slot = SlotWork.builder()
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        return toResponse(slotWorkRepository.save(slot));
    }

    // ================= GET ALL =================
    public List<SlotWorkResponse> getAll() {
        return slotWorkRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ================= GET BY ID =================
    public SlotWorkResponse getById(Integer id) {
        SlotWork slot = slotWorkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        return toResponse(slot);
    }

    // ================= UPDATE =================
    public SlotWorkResponse update(Integer id, UpdateSlotWorkRequest request) {

        SlotWork slot = slotWorkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (request.getStartTime() != null) {
            slot.setStartTime(request.getStartTime());
        }

        if (request.getEndTime() != null) {
            slot.setEndTime(request.getEndTime());
        }

        if (slot.getStartTime().isAfter(slot.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        return toResponse(slotWorkRepository.save(slot));
    }

    // ================= DELETE =================
    public void delete(Integer id) {
        if (!slotWorkRepository.existsById(id)) {
            throw new RuntimeException("Slot not found");
        }
        slotWorkRepository.deleteById(id);
    }

    // ================= MAPPER =================
    private SlotWorkResponse toResponse(SlotWork entity) {
        SlotWorkResponse res = new SlotWorkResponse();
        res.setId(entity.getId());
        res.setStartTime(entity.getStartTime());
        res.setEndTime(entity.getEndTime());
        return res;
    }
}