package hotel_booking.controller;

import hotel_booking.dto.request.*;
import hotel_booking.dto.response.SalaryBatchResponse;
import hotel_booking.entity.Salary;
import hotel_booking.service.SalaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/salaries")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;

    @PostMapping("/add")
    public ResponseEntity<?> createSalary(@RequestBody CreateSalaryRequest request) {
        return ResponseEntity.ok(
                salaryService.createSalary(request)
        );
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<?> updateSalary(
            @PathVariable Long id,
            @RequestBody UpdateSalaryRequest request
    ) {

        return ResponseEntity.ok(
                salaryService.updateSalary(id, request)
        );
    }

    @GetMapping
    public ResponseEntity<?> getAllSalaries(
            @ModelAttribute PaginationRequest req
    ) {

        return ResponseEntity.ok(
                salaryService.getAllSalaries(req)
        );
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<?> getSalaryById(@PathVariable Long id) {

        return ResponseEntity.ok(
                salaryService.getSalaryById(id)
        );
    }

    @PostMapping("/bonus")
    public ResponseEntity<?> addBonus(@RequestBody BonusRequest req) {

        salaryService.addBonus(req);

        return ResponseEntity.ok("Bonus added successfully");
    }

    @PostMapping("/adjust-batch-all")
    public ResponseEntity<?> adjustBatch(@RequestBody AdjustBatchRequest req) {

        salaryService.adjustByBatch(
                req.getBatchId(),
                req.getAmount(),
                req.getNote()
        );

        return ResponseEntity.ok("Adjust batch success");
    }

    @PostMapping("/adjust")
    public ResponseEntity<?> adjustSingle(@RequestBody AdjustRequest req) {

        salaryService.adjustSingle(
                req.getRecordId(),
                req.getAmount(),
                req.getNote()
        );

        return ResponseEntity.ok("Adjust success");
    }

    @GetMapping("/batches")
    public ResponseEntity<?> getSalaryBatches(
            @RequestParam String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Page<SalaryBatchResponse> result =
                salaryService.getSalaryBatches(type, workDate, page, size);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/run-daily")
    public ResponseEntity<?> runDailySalary(
            @RequestParam(required = false) String date
    ) {
        LocalDate workDate = (date == null)
                ? null
                : LocalDate.parse(date);

        salaryService.calculateDailySalary(workDate);

        return ResponseEntity.ok("Done!");
    }

    @GetMapping("/daily")
    public ResponseEntity<?> getDailySalaries(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate workDate,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(
                salaryService.getDailySalaries(workDate, page, size)
        );
    }

    @PostMapping("/calculate-monthly")
    public ResponseEntity<?> calculateMonthlySalary(
            @RequestParam Integer userId,
            @RequestParam int month,
            @RequestParam int year
    ) {
        Salary salary = salaryService
                .calculateMonthlySalary(userId, month, year);

        return ResponseEntity.ok(salary);
    }

    @PostMapping("/calculate-all")
    public ResponseEntity<?> calculateAll(
            @RequestParam int month,
            @RequestParam int year
    ) {
        salaryService.calculateAllUsers(month, year);
        return ResponseEntity.ok("Done");
    }

    @GetMapping("/salaries")
    public ResponseEntity<?> getSalaries(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(
                salaryService.getSalaries(month, year, status, page, size)
        );
    }

    @GetMapping("/detail")
    public ResponseEntity<?> getSalaryDetail(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(
                salaryService.getSalaryDetail(userId, month, year)
        );
    }

    @PutMapping("/status")
    public ResponseEntity<Salary> updateStatus(
            @RequestParam Long userId,
            @RequestParam Integer month,
            @RequestParam Integer year,
            @RequestParam Integer status
    ) {
        Salary updatedSalary = salaryService.updateSalaryStatus(userId, month, year, status);
        return ResponseEntity.ok(updatedSalary);
    }

}
