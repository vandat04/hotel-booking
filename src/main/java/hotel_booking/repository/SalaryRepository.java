package hotel_booking.repository;

import hotel_booking.dto.response.SalaryResponse;
import hotel_booking.entity.Salary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Long> {

    Optional<Salary> findByUserIdAndSalaryMonthAndSalaryYear(
            Long userId, Integer month, Integer year);

    Optional<Salary> findByUserIdAndSalaryMonthAndSalaryYear(
            Integer userId, int month, int year
    );

    @Query("""
                SELECT new hotel_booking.dto.response.SalaryResponse(
                    s.userId,
                    s.totalSalary,
                    s.salaryMonth,
                    s.salaryYear,
                    s.status,
                    s.attendance
                )
                FROM Salary s
                WHERE s.salaryMonth = :month
                  AND s.salaryYear = :year
                  AND (:status IS NULL OR s.status = :status)
            """)
    Page<SalaryResponse> findSalaries(
            int month,
            int year,
            Integer status,
            Pageable pageable
    );

    Optional<Salary> findByUserIdAndSalaryMonthAndSalaryYear(
            Long userId, int month, int year
    );


}
