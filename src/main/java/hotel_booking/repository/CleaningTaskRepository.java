package hotel_booking.repository;

import hotel_booking.entity.CleaningTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CleaningTaskRepository extends JpaRepository<CleaningTask, Long> {

    List<CleaningTask> findByRoomId(Long roomId);

    List<CleaningTask> findByCleanerId(Long cleanerId);

    // 🔥 filter theo status + cleaner + phân trang
    Page<CleaningTask> findByCleanerIdAndStatus(
            Long cleanerId,
            String status,
            Pageable pageable
    );

    // 🔥 không filter status
    Page<CleaningTask> findByCleanerId(
            Long cleanerId,
            Pageable pageable
    );

    long countByCleanerId(Long cleanerId);

    long countByCleanerIdAndStatus(Long cleanerId, String status);

    long countByCleanerIdAndCreatedAtBetween(
            Long cleanerId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query(value = """
                SELECT * FROM CleaningTasks ct
                WHERE (:status IS NULL OR ct.status = :status)
                  AND (:date IS NULL OR CAST(ct.created_at AS DATE) = :date)
            """,
            countQuery = """
                        SELECT COUNT(*) FROM CleaningTasks ct
                        WHERE (:status IS NULL OR ct.status = :status)
                          AND (:date IS NULL OR CAST(ct.created_at AS DATE) = :date)
                    """,
            nativeQuery = true)
    Page<CleaningTask> searchCleaningTasks(
            @Param("status") String status,
            @Param("date") LocalDate date,
            Pageable pageable
    );
}
