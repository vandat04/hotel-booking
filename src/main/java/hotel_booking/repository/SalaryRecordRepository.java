package hotel_booking.repository;

import hotel_booking.dto.response.SalaryBatchResponse;
import hotel_booking.entity.SalaryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, Long> {

    List<SalaryRecord> findByUserIdAndWorkDate(Long userId, LocalDate workDate);

    List<SalaryRecord> findByBatchId(String batchId);

    @Query("""
                SELECT new hotel_booking.dto.response.SalaryBatchResponse(
                    sr.batchId,
                    sr.type,
                    SUM(sr.amount),
                    COUNT(DISTINCT sr.userId),
                    sr.workDate
                )
                FROM SalaryRecord sr
                WHERE sr.type = 'BONUS'
                  AND sr.workDate = :workDate
                GROUP BY sr.batchId, sr.type, sr.workDate
            """)
    Page<SalaryBatchResponse> getBonusBatches(LocalDate workDate, Pageable pageable);

    @Query("""
                SELECT new hotel_booking.dto.response.SalaryBatchResponse(
                    sr.note,
                    sr.type,
                    SUM(sr.amount),
                    COUNT(DISTINCT sr.userId),
                    sr.workDate
                )
                FROM SalaryRecord sr
                WHERE sr.type = 'ADJUSTMENT'
                  AND sr.workDate = :workDate
                GROUP BY sr.note, sr.type, sr.workDate
            """)
    Page<SalaryBatchResponse> getAdjustmentBatches(LocalDate workDate, Pageable pageable);

    boolean existsByUserIdAndWorkDateAndType(Integer userId, LocalDate workDate, String type);

    @Query("""
                SELECT sr FROM SalaryRecord sr
                WHERE sr.type = 'SALARY'
                  AND sr.workDate = :workDate
            """)
    Page<SalaryRecord> getDailySalaries(LocalDate workDate, Pageable pageable);

    @Query("""
                SELECT COALESCE(SUM(sr.amount), 0)
                FROM SalaryRecord sr
                WHERE sr.userId = :userId
                  AND MONTH(sr.workDate) = :month
                  AND YEAR(sr.workDate) = :year
            """)
    BigDecimal sumSalaryRecords(Integer userId, int month, int year);

}
