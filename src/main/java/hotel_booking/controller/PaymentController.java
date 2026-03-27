package hotel_booking.controller;

import hotel_booking.dto.request.PaymentRequest;
import hotel_booking.dto.response.UserProfileResponse;
import hotel_booking.security.CustomUserDetails;
import hotel_booking.service.PaymentService;
import hotel_booking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/hotel/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping("/vnpay")
    public String createPayment(
            @RequestBody PaymentRequest req,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 🔥 lấy userId từ principal (tuỳ bạn set trong JWT)
        Long userId = Long.parseLong(auth.getName());

        UserProfileResponse userp = userService.getMyProfile();

        return paymentService.createVnPayPayment(userp.getId().longValue(), req);
    }

    @GetMapping("/vnpay-return")
    public String vnpayReturn(@RequestParam Map<String, String> params) {
        paymentService.handleVnPayReturn(params);
        return "redirect:/payment-result";
    }
}
