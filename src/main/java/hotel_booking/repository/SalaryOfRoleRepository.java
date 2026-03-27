package hotel_booking.repository;


import hotel_booking.entity.SalaryOfRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalaryOfRoleRepository extends JpaRepository<SalaryOfRole, Long> {

    boolean existsByRole(String role);

    Optional<SalaryOfRole> findByRole(String role);

}
