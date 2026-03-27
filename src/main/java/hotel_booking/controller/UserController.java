package hotel_booking.controller;

import hotel_booking.dto.request.CreateStaffRequest;
import hotel_booking.dto.request.PaginationRequest;
import hotel_booking.dto.request.UpdateUserRequest;
import hotel_booking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/staff")
    public ResponseEntity<?> createStaff(@RequestBody CreateStaffRequest request) {
        return ResponseEntity.ok(userService.createStaff(request));
    }

    @GetMapping
    public ResponseEntity<?> getUsers(
            @ModelAttribute PaginationRequest req,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status
    ) {

        return ResponseEntity.ok(
                userService.getUsers(req, role, status)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {

        return ResponseEntity.ok(
                userService.getMyProfile()
        );
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {

        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(
                userService.updateUser(id, request)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<?> getUsers(
            @ModelAttribute PaginationRequest req,
            @RequestParam(required = false) String keyword
    ) {

        return ResponseEntity.ok(
                userService.searchUsers(req, keyword)
        );
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok(
                userService.getUserDashboard()
        );
    }
}
