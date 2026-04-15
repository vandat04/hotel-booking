package hotel_booking.service;

import hotel_booking.dto.request.*;
import hotel_booking.dto.response.*;
import hotel_booking.entity.*;
import hotel_booking.repository.*;
import hotel_booking.util.PaginationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SalaryService {

    private final SalaryOfRoleRepository salaryOfRoleRepository;
    private final UserRepository userRepository;
    private final SalaryRecordRepository salaryRecordRepository;
    private final AttendanceRepository attendanceRepository;
    private final PenaltyRepository penaltyRepository;
    private final SalaryRepository salaryRepository;

    public SalaryOfRole createSalary(CreateSalaryRequest request) {

        // 🔥 1. Validate role
        if (request.getRole() == null || request.getRole().isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }

        String role = request.getRole().toUpperCase();

        if (!role.equals("ADMIN") &&
                !role.equals("CLEANER") &&
                !role.equals("RECEPTIONIST")) {
            throw new IllegalArgumentException("Invalid role");
        }

        // 🔥 2. Check duplicate
        if (salaryOfRoleRepository.existsByRole(role)) {
            throw new RuntimeException("Salary already exists for role: " + role);
        }

        // 🔥 3. Validate salary
        if (request.getSalary() == null ||
                request.getSalary().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Salary must be > 0");
        }

        // 🔥 4. Create entity
        SalaryOfRole salary = new SalaryOfRole();
        salary.setRole(role);
        salary.setSalary(request.getSalary());
        salary.setCreatedAt(LocalDateTime.now());

        return salaryOfRoleRepository.save(salary);
    }

    public SalaryOfRole updateSalary(Long id, UpdateSalaryRequest request) {

        // 🔥 1. Tìm record
        SalaryOfRole salary = salaryOfRoleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salary not found"));

        // 🔥 2. Update role
        if (request.getRole() != null && !request.getRole().isBlank()) {

            String newRole = request.getRole().toUpperCase();

            // validate role
            if (!newRole.equals("ADMIN") &&
                    !newRole.equals("CLEANER") &&
                    !newRole.equals("RECEPTIONIST")) {
                throw new IllegalArgumentException("Invalid role");
            }

            // check duplicate (trừ chính nó)
            boolean exists = salaryOfRoleRepository.existsByRole(newRole);
            if (exists && !salary.getRole().equals(newRole)) {
                throw new RuntimeException("Role already exists: " + newRole);
            }

            salary.setRole(newRole);
        }

        // 🔥 3. Update salary
        if (request.getSalary() != null) {

            if (request.getSalary().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Salary must be > 0");
            }

            salary.setSalary(request.getSalary());
        }

        // 🔥 4. Update time
        salary.setUpdatedAt(java.time.LocalDateTime.now());

        // 🔥 5. Save
        return salaryOfRoleRepository.save(salary);
    }

    public Page<SalaryOfRoleResponse> getAllSalaries(PaginationRequest req) {

        // 🔥 default sort theo role A → Z
        if (req.getSortBy() == null || req.getSortBy().isBlank()) {
            req.setSortBy("role");
            req.setDirection("asc");
        }

        Pageable pageable = PaginationUtil.build(req);

        Page<SalaryOfRole> result = salaryOfRoleRepository.findAll(pageable);

        return result.map(this::toResponse);
    }

    public SalaryOfRoleResponse getSalaryById(Long id) {

        SalaryOfRole salary = salaryOfRoleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salary not found"));

        return toResponse(salary);
    }

    public String addBonus(BonusRequest req) {

        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be > 0");
        }

        LocalDate workDate = (req.getWorkDate() != null)
                ? req.getWorkDate()
                : LocalDate.now();

        // 🔥 tạo batchId chung
        String batchId = UUID.randomUUID().toString();

        // ================= BONUS 1 NGƯỜI =================
        if (req.getUserId() != null) {

            User user = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            validateStaff(user);

            SalaryRecord record = new SalaryRecord();
            record.setUserId(user.getId().longValue());
            record.setWorkDate(workDate);
            record.setAmount(req.getAmount());
            record.setType("BONUS");
            record.setNote(req.getNote());
            record.setBatchId(batchId);

            salaryRecordRepository.save(record);
        }

        // ================= BONUS ALL =================
        else {

            List<User> staffs = userRepository
                    .findByRoleIn(List.of("CLEANER", "RECEPTIONIST"));

            for (User user : staffs) {

                SalaryRecord record = new SalaryRecord();
                record.setUserId(user.getId().longValue());
                record.setWorkDate(workDate);
                record.setAmount(req.getAmount());
                record.setType("BONUS");
                record.setNote(req.getNote());
                record.setBatchId(batchId);

                salaryRecordRepository.save(record);
            }
        }

        return batchId; // 🔥 cực kỳ quan trọng để rollback sau này
    }

    //Chỉnh sửa hàng loạt
    public void adjustByBatch(String batchId, BigDecimal amount, String note) {

        List<SalaryRecord> records = salaryRecordRepository.findByBatchId(batchId);

        if (records.isEmpty()) {
            throw new RuntimeException("Batch not found");
        }

        for (SalaryRecord original : records) {

            SalaryRecord adjust = new SalaryRecord();
            adjust.setUserId(original.getUserId());
            adjust.setWorkDate(original.getWorkDate());

            // 🔥 LUÔN là số âm để rollback
            adjust.setAmount(amount.negate());

            adjust.setType("ADJUSTMENT");
            adjust.setNote("Adjust batch " + batchId + " - " + note);

            salaryRecordRepository.save(adjust);
        }
    }
    //Chỉnh sửa cho 1 người
    public void adjustSingle(Long recordId, BigDecimal amount, String note) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be > 0");
        }

        SalaryRecord original = salaryRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        SalaryRecord adjust = new SalaryRecord();
        adjust.setUserId(original.getUserId());
        adjust.setWorkDate(original.getWorkDate());

        // 🔥 luôn trừ (rollback)
        adjust.setAmount(amount.negate());

        adjust.setType("ADJUSTMENT");
        adjust.setNote("Adjust record #" + recordId + " - " + note);

        salaryRecordRepository.save(adjust);
    }

    public Page<SalaryBatchResponse> getSalaryBatches(
            String type,
            LocalDate workDate,
            int page,
            int size
    ) {
        if (workDate == null) {
            workDate = LocalDate.now();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("workDate").descending());

        if ("BONUS".equalsIgnoreCase(type)) {
            return salaryRecordRepository.getBonusBatches(workDate, pageable);
        } else if ("ADJUSTMENT".equalsIgnoreCase(type)) {
            return salaryRecordRepository.getAdjustmentBatches(workDate, pageable);
        } else {
            throw new IllegalArgumentException("Type must be BONUS or ADJUSTMENT");
        }
    }

    @Transactional
    public void calculateDailySalary(LocalDate workDate) {

        if (workDate == null) {
            workDate = LocalDate.now().minusDays(1);
        }

        List<Attendance> attendances =
                attendanceRepository.findValidAttendance(workDate);

        String batchId = "SALARY_" + workDate;

        for (Attendance att : attendances) {
            Integer userId = att.getUserId().intValue();

            if (salaryRecordRepository.existsByUserIdAndWorkDateAndType(
                    userId, workDate, "SALARY")) {
                continue;
            }

            User user = userRepository.findById(userId).orElseThrow();
            SalaryOfRole salaryOfRole =
                    salaryOfRoleRepository.findByRole(user.getRole()).orElseThrow();

            BigDecimal amount = salaryOfRole.getSalary();

            if (att.getStatus() == 3) {
                amount = amount.multiply(BigDecimal.valueOf(0.8));
            }

            SalaryRecord record = new SalaryRecord();
            record.setUserId(userId.longValue());
            record.setWorkDate(workDate);
            record.setAmount(amount);
            record.setType("SALARY");
            record.setNote("Daily salary");
            record.setBatchId(batchId);

            salaryRecordRepository.save(record);
        }
    }

    public void calculateDailySalary() {
        LocalDate workDate = LocalDate.now().minusDays(1);
        calculateDailySalary(workDate);
    }

    public Page<SalaryRecord> getDailySalaries(
            LocalDate workDate,
            int page,
            int size
    ) {
        if (workDate == null) {
            workDate = LocalDate.now();
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("workDate").descending()
        );

        return salaryRecordRepository.getDailySalaries(workDate, pageable);
    }

    @Transactional
    public Salary calculateMonthlySalary(
            Integer userId,
            int month,
            int year
    ) {

        // 1. Tổng thu nhập
        BigDecimal totalIncome =
                salaryRecordRepository.sumSalaryRecords(userId, month, year);

        // 2. Tổng tiền phạt
        BigDecimal totalPenalty =
                penaltyRepository.sumPenalties(userId, month, year);

        // 3. Số buổi đi làm
        Integer totalAttendance =
                attendanceRepository.countAttendance(userId.longValue(), month, year);

        if (totalAttendance == null) {
            totalAttendance = 0;
        }

        // 4. Lương cuối
        BigDecimal finalSalary = totalIncome.subtract(totalPenalty);

        // 5. Check tồn tại
        Salary salary = salaryRepository
                .findByUserIdAndSalaryMonthAndSalaryYear(userId, month, year)
                .orElse(null);

        if (salary == null) {
            // 👉 INSERT
            salary = new Salary();
            salary.setUserId(userId.longValue());
            salary.setSalaryMonth(month);
            salary.setSalaryYear(year);
            salary.setTotalSalary(finalSalary);
            salary.setAttendance(totalAttendance);
            salary.setStatus(1);
        } else {
            // 👉 UPDATE (re-calc)
            if (salary.getStatus() == 2) {
                throw new RuntimeException("Salary already paid, cannot update!");
            }

            salary.setTotalSalary(finalSalary);
            salary.setAttendance(totalAttendance);
        }

        return salaryRepository.save(salary);
    }

    public void calculateAllUsers(int month, int year) {

        List<User> users = userRepository.findByRoleIn(
                List.of("CLEANER", "RECEPTIONIST")
        );

        for (User user : users) {
            calculateMonthlySalary(user.getId(), month, year);
        }
    }

    public Page<SalaryResponse> getSalaries(
            Integer month,
            Integer year,
            Integer status,
            int page,
            int size
    ) {
        LocalDate now = LocalDate.now();

        if (month == null) {
            month = now.getMonthValue();
        }

        if (year == null) {
            year = now.getYear();
        }

        if (status == null) {
            status = 1;
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("totalSalary").descending()
        );

        return salaryRepository.findSalaries(
                month,
                year,
                status,
                pageable
        );
    }

    public SalaryDetailResponse getSalaryDetail(
            Long userId,
            Integer month,
            Integer year
    ) {

        LocalDate now = LocalDate.now();

        // default tháng/năm hiện tại
        if (month == null) {
            month = now.getMonthValue();
        }

        if (year == null) {
            year = now.getYear();
        }

        // 1. Lấy salary
        Salary salary = salaryRepository
                .findByUserIdAndSalaryMonthAndSalaryYear(userId, month, year)
                .orElseThrow(() -> new RuntimeException("Salary not found"));

        // 2. Lấy user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Map SalaryResponse
        SalaryResponse salaryResponse = new SalaryResponse(
                salary.getUserId(),
                salary.getTotalSalary(),
                salary.getSalaryMonth(),
                salary.getSalaryYear(),
                salary.getStatus(),
                salary.getAttendance()
        );

        // 4. Map UserProfileResponse
        UserProfileResponse userResponse = UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();

        // 5. Return
        return new SalaryDetailResponse(salaryResponse, userResponse);
    }

    public Salary updateSalaryStatus(Long userId, Integer month, Integer year, Integer status) {
        if (status != 2) {
            throw new IllegalArgumentException("Chỉ có thể cập nhật trạng thái sang 'đã trả' (2)");
        }

        Salary salary = salaryRepository.findByUserIdAndSalaryMonthAndSalaryYear(userId, month, year)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy bảng lương cho userId=" + userId + ", tháng=" + month + ", năm=" + year));

        if (salary.getStatus() == 2) {
            throw new RuntimeException("Bảng lương này đã được thanh toán, không thể cập nhật lại.");
        }

        // Chỉ cập nhật từ 1 → 2
        salary.setStatus(2);
        return salaryRepository.save(salary);
    }

    private void validateStaff(User user) {
        if (!user.getRole().equals("CLEANER") &&
                !user.getRole().equals("RECEPTIONIST")) {
            throw new RuntimeException("Only staff can receive bonus");
        }
    }

    private SalaryOfRoleResponse toResponse(SalaryOfRole entity) {
        SalaryOfRoleResponse dto = new SalaryOfRoleResponse();
        dto.setId(entity.getId());
        dto.setRole(entity.getRole());
        dto.setSalary(entity.getSalary());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}