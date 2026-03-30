package hotel_booking.service;

import hotel_booking.dto.response.CleanTaskDetailResponse;
import hotel_booking.entity.Booking;
import hotel_booking.entity.CleaningTask;
import hotel_booking.repository.BookingRepository;
import hotel_booking.repository.CleaningTaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CleanTaskService {

    @Autowired
    private CleaningTaskRepository cleanTaskRepository;
    private final BookingRepository bookingRepository;

    public Page<CleaningTask> getTasks(
            Long cleanerId,
            String status,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending() // 🔥 mới → cũ
        );

        if (status != null && !status.isEmpty()) {
            return cleanTaskRepository.findByCleanerIdAndStatus(
                    cleanerId,
                    status,
                    pageable
            );
        }

        return cleanTaskRepository.findByCleanerId(cleanerId, pageable);
    }

    @Transactional
    public String completeTask(Long taskId, Long cleanerId) {

        // 🔥 1. Lấy task
        CleaningTask task = cleanTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // 🔥 2. Check quyền (task phải của cleaner này)
        if (!task.getCleanerId().equals(cleanerId)) {
            throw new RuntimeException("Bạn không có quyền với task này");
        }

        // 🔥 3. Check trạng thái hợp lệ
        if ("DONE".equals(task.getStatus())) {
            throw new RuntimeException("Task đã hoàn thành rồi");
        }

        // 🔥 4. Update task → DONE
        task.setStatus("DONE");
        cleanTaskRepository.save(task);

        // 🔥 5. Update booking → CHECKED_OUT
        if (task.getBookingId() != null) {

            Booking booking = bookingRepository.findById(task.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            booking.setStatus("CHECKED_OUT"); // 🔥 đúng theo DB của bạn

            bookingRepository.save(booking);
        }

        return "Hoàn thành task và cập nhật booking thành CHECKED_OUT";
    }

    public CleanTaskDetailResponse getTaskDetail(Long taskId, Long cleanerId) {

        // 🔥 1. Lấy task
        CleaningTask task = cleanTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task không tồn tại"));

        // 🔥 2. Check quyền (chỉ cleaner của task mới xem được)
        if (!task.getCleanerId().equals(cleanerId)) {
            throw new RuntimeException("Bạn không có quyền xem task này");
        }

        Booking booking = null;

        // 🔥 3. Lấy booking nếu có
        if (task.getBookingId() != null) {
            booking = bookingRepository.findById(task.getBookingId())
                    .orElse(null);
        }

        // 🔥 4. Map sang DTO
        CleanTaskDetailResponse res = new CleanTaskDetailResponse();

        res.setId(task.getId());
        res.setStatus(task.getStatus());
        res.setCleanerId(task.getCleanerId());
        res.setCreatedAt(task.getCreatedAt());

        if (booking != null) {
            res.setBookingId(booking.getId());
            res.setBookingStatus(booking.getStatus());
            res.setCheckIn(booking.getCheckIn());
            res.setCheckOut(booking.getCheckOut());
        }

        return res;
    }

    public Page<CleaningTask> getCleaningTasks(
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

        return cleanTaskRepository.searchCleaningTasks(status, date, pageable);
    }

}
