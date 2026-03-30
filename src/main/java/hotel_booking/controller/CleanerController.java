package hotel_booking.controller;

import hotel_booking.entity.CleaningTask;
import hotel_booking.service.CleanTaskService;
import hotel_booking.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/cleaner")
@RequiredArgsConstructor
public class CleanerController {
    @Autowired
    private CleanTaskService cleanTaskService;

    @GetMapping("/tasks")
    public ResponseEntity<?> getTasks(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {

        // 🔥 lấy userId từ SecurityContext
        Long cleanerId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Page<CleaningTask> tasks = cleanTaskService.getTasks(
                cleanerId,
                status,
                page,
                size
        );

        return ResponseEntity.ok(tasks);
    }

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {

        Long cleanerId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return ResponseEntity.ok(
                dashboardService.getCleanerDashboard(cleanerId)
        );
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<?> getTaskDetail(@PathVariable Long id) {

        Long cleanerId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return ResponseEntity.ok(
                cleanTaskService.getTaskDetail(id, cleanerId)
        );
    }

    @PutMapping("/tasks/{id}/complete")
    public ResponseEntity<?> completeTask(@PathVariable Long id) {

        Long cleanerId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return ResponseEntity.ok(
                cleanTaskService.completeTask(id, cleanerId)
        );
    }
}
