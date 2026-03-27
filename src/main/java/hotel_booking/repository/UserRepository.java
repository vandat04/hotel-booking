package hotel_booking.repository;

import hotel_booking.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Integer id);

    Optional<User> findByUsername(String username);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    Optional<User> findByEmailOrUsername(String email, String username);

    Page<User> findByRoleAndStatus(String role, Integer status, Pageable pageable);

    Page<User> findByRole(String role, Pageable pageable);

    Page<User> findByStatus(Integer status, Pageable pageable);

    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(String username, String email, String phone, Pageable pageable);

    long countByRole(String role);

    long countByStatus(Integer status);

    // lấy user mới nhất
    List<User> findTop5ByOrderByCreatedAtDesc();

    List<User> findByRoleIn(List<String> roles);

}