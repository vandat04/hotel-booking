package hotel_booking.repository;

import hotel_booking.entity.Penalty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, Long> {

    List<Penalty> findByUserIdAndWorkDate(Long userId, LocalDate workDate);

    Page<Penalty> findByWorkDate(LocalDate workDate, Pageable pageable);

    Page<Penalty> findByWorkDateAndUserId(LocalDate workDate, Long userId, Pageable pageable);

    @Query("""
                SELECT COALESCE(SUM(p.amount), 0)
                FROM Penalty p
                WHERE p.userId = :userId
                  AND MONTH(p.workDate) = :month
                  AND YEAR(p.workDate) = :year
            """)
    BigDecimal sumPenalties(Integer userId, int month, int year);

}