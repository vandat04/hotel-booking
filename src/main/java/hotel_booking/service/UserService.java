package hotel_booking.service;

import hotel_booking.dto.request.*;
import hotel_booking.dto.response.UserDashboardResponse;
import hotel_booking.dto.response.UserProfileResponse;
import hotel_booking.entity.User;
import hotel_booking.repository.UserRepository;
import hotel_booking.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;

    // =============================
    public void updateProfile(UpdateProfileRequest request, MultipartFile file) {

        String currentUserId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Integer userId = Integer.parseInt(currentUserId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // =============================
        // update email
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {

            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());

            if (existingUser.isPresent()) {
                throw new RuntimeException("Email đã tồn tại");
            }

            user.setEmail(request.getEmail());
        }
        // update phone
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {

            Optional<User> existingUser = userRepository.findByPhone(request.getPhone());

            if (existingUser.isPresent()) {
                throw new RuntimeException("Phone numeber đã tồn tại");
            }

            user.setPhone(request.getPhone());
        }

        // =============================
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        // =============================
        //  UPLOAD AVATAR
        if (file != null && !file.isEmpty()) {

            String imageUrl = cloudinaryService.uploadFile1(file);

            user.setAvatarUrl(imageUrl);
        }

        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    // =============================
    public UserProfileResponse getProfile() {

        String currentUserId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Integer userId = Integer.parseInt(currentUserId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        return UserProfileResponse.builder()
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
    }

    // =============================
    public void changePassword(Long userId, ChangePasswordRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check old password
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // Check newPassword == confirmPassword
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    public User createStaff(CreateStaffRequest request) {

        // 🔥 1. Validate role
        String role = request.getRole().toUpperCase();

        if (!role.equals("CLEANER") && !role.equals("RECEPTIONIST")) {
            throw new IllegalArgumentException("Only CLEANER or RECEPTIONIST allowed");
        }

        // 🔥 2. Check duplicate
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone already exists");
        }

        // 🔥 3. Encode password
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 🔥 4. Create user
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(encodedPassword)
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(role)
                .status(1) // ACTIVE
                .provider("LOCAL")
                .build();

        return userRepository.save(user);
    }

    public Page<UserProfileResponse> getUsers(
            PaginationRequest req,
            String role,
            Integer status
    ) {

        // 🔥 default sort theo createdAt mới nhất
        if (req.getSortBy() == null || req.getSortBy().isBlank()) {
            req.setSortBy("createdAt");
            req.setDirection("desc");
        }

        Pageable pageable = PaginationUtil.build(req);

        Page<User> result;

        String roleFilter = (role != null) ? role.toUpperCase().trim() : null;

        // validate role
        if (roleFilter != null &&
                !roleFilter.equals("CLEANER") &&
                !roleFilter.equals("RECEPTIONIST") &&
                !roleFilter.equals("CUSTOMER")) {
            throw new IllegalArgumentException("Invalid role");
        }

        // validate status
        if (status != null && (status < 1 || status > 3)) {
            throw new IllegalArgumentException("Status must be 1,2,3");
        }

        // ================= FILTER =================
        if (roleFilter != null && status != null) {
            result = userRepository.findByRoleAndStatus(roleFilter, status, pageable);

        } else if (roleFilter != null) {
            result = userRepository.findByRole(roleFilter, pageable);

        } else if (status != null) {
            result = userRepository.findByStatus(status, pageable);

        } else {
            result = userRepository.findAll(pageable);
        }

        return result.map(this::toResponse);
    }

    public UserProfileResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return toResponse(user);
    }

    public UserProfileResponse getMyProfile() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 🔥 lấy userId từ principal (tuỳ bạn set trong JWT)
        Long userId = Long.parseLong(auth.getName());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return toResponse(user);
    }

    public UserProfileResponse updateUser(Long id, UpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // update từng field nếu có
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            // check trùng phone
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new RuntimeException("Phone already exists");
            }
            user.setPhone(request.getPhone());
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (request.getStatus() != null) {
            if (request.getStatus() < 1 || request.getStatus() > 3) {
                throw new IllegalArgumentException("Status must be 1, 2, or 3");
            }
            user.setStatus(request.getStatus());
        }

        user.setUpdatedAt(LocalDateTime.now());

        User updated = userRepository.save(user);

        return toResponse(updated);
    }

    public Page<UserProfileResponse> searchUsers(
            PaginationRequest req,
            String search
    ) {

        // 🔥 default sort (A → Z theo username)
        if (req.getSortBy() == null || req.getSortBy().isBlank()) {
            req.setSortBy("username");
            req.setDirection("asc");
        }

        Pageable pageable = PaginationUtil.build(req);

        Page<User> result;

        String keyword = (search != null) ? search.trim() : "";

        if (!keyword.isBlank()) {
            result = userRepository
                    .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(
                            keyword, keyword, keyword, pageable
                    );
        } else {
            result = userRepository.findAll(pageable);
        }

        return result.map(this::toResponse);
    }

    public UserDashboardResponse getUserDashboard() {

        UserDashboardResponse res = new UserDashboardResponse();

        // ===== Tổng =====
        res.setTotalUsers(userRepository.count());

        // ===== Theo ROLE =====
        res.setTotalAdmin(userRepository.countByRole("ADMIN"));
        res.setTotalCleaner(userRepository.countByRole("CLEANER"));
        res.setTotalReceptionist(userRepository.countByRole("RECEPTIONIST"));
        res.setTotalCustomer(userRepository.countByRole("CUSTOMER"));

        // ===== Theo STATUS =====
        res.setActiveUsers(userRepository.countByStatus(1));
        res.setInactiveUsers(userRepository.countByStatus(2));
        res.setBannedUsers(userRepository.countByStatus(3));

        // ===== User mới =====
        List<UserProfileResponse> recent = userRepository
                .findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();

        res.setRecentUsers(recent);

        return res;
    }

    private UserProfileResponse toResponse(User entity) {
        UserProfileResponse dto = new UserProfileResponse();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setFullName(entity.getFullName());
        dto.setPhone(entity.getPhone());
        dto.setAvatarUrl(entity.getAvatarUrl());
        dto.setRole(entity.getRole());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
