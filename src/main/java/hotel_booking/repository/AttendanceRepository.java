package hotel_booking.repository;

import hotel_booking.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByUserIdAndWorkDate(Integer userId, LocalDate workDate);

    @Query("""
                SELECT a FROM Attendance a
                WHERE a.workDate = :workDate
                  AND a.status IN (1, 3)
            """)
    List<Attendance> findValidAttendance(LocalDate workDate);

    @Query("""
                SELECT COUNT(a)
                FROM Attendance a
                WHERE a.userId = :userId
                  AND a.status IN (1, 3)
                  AND MONTH(a.workDate) = :month
                  AND YEAR(a.workDate) = :year
            """)
    Integer countAttendance(Integer userId, int month, int year);

    @Query(value = """
                SELECT * FROM Attendance a
                WHERE (:date IS NULL OR a.work_date = :date)
                  AND (:slotId IS NULL OR a.slot_id = :slotId)
            """,
            countQuery = """
                        SELECT COUNT(*) FROM Attendance a
                        WHERE (:date IS NULL OR a.work_date = :date)
                          AND (:slotId IS NULL OR a.slot_id = :slotId)
                    """,
            nativeQuery = true)
    Page<Attendance> searchAttendance(
            @Param("date") LocalDate date,
            @Param("slotId") Integer slotId,
            Pageable pageable
    );


}
