package hotel_booking.controller;

import hotel_booking.dto.request.ChangePasswordRequest;
import hotel_booking.dto.request.UpdateProfileRequest;
import hotel_booking.dto.response.UserProfileResponse;
import hotel_booking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.spec.PSource;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final UserService userService;

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(
            @RequestPart("data") UpdateProfileRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {

        userService.updateProfile(request, file);

        return ResponseEntity.ok("Cập nhật profile thành công");
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        UserProfileResponse profile = userService.getProfile();
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req, Principal principal) {
        // Lấy userId từ SecurityContext / JWT
        Long userId = Long.parseLong(principal.getName());

        userService.changePassword(userId, req);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

}
