package hotel_booking.filter;

import hotel_booking.repository.InvalidTokenRepository;
import hotel_booking.security.CustomUserDetailsService;
import hotel_booking.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private InvalidTokenRepository invalidTokenRepository; // ✅ inject

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);
        // 🔥 check blacklist
        if (invalidTokenRepository.existsByToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token đã bị vô hiệu hóa");
            return;
        }
        // 🔥 lấy userId từ JWT
        String userIdStr = jwtService.extractUserId(token);

        if (userIdStr != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtService.isValid(token)) {

                Long userId = Long.parseLong(userIdStr);
                UserDetails userDetails = userDetailsService.loadUserById(userId.intValue());

                System.out.println(userDetails.getAuthorities());// 🔥 KHÔNG cần load DB nữa
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userId,   // 🔥 principal = userId
                                null,
                                userDetails.getAuthorities()      // hoặc authorities nếu có
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}