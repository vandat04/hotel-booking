package hotel_booking.repository;

import hotel_booking.entity.RoomSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomScheduleRepository extends JpaRepository<RoomSchedule, Long> {

    @Query("""
                SELECT DISTINCT rs.roomId
                FROM RoomSchedule rs
                WHERE rs.roomId IN :roomIds
                AND rs.status IN ('BOOKED', 'OCCUPIED', 'BLOCKED')
                AND rs.startTime < :checkOut
                AND rs.endTime > :checkIn
            """)
    List<Long> findOccupiedRoomIds(
            List<Long> roomIds,
            LocalDateTime checkIn,
            LocalDateTime checkOut
    );

    void deleteByBookingId(Long bookingId);

    // 🔹 Kiểm tra có conflict với khoảng thời gian extend không
    boolean existsByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long roomId,
            LocalDateTime newEndTime,
            LocalDateTime oldStartTime
    );

    // 🔹 Lấy schedule cuối cùng của phòng cho booking
    @Query("SELECT rs FROM RoomSchedule rs " +
            "WHERE rs.roomId = :roomId AND rs.bookingId = :bookingId " +
            "ORDER BY rs.endTime DESC")
    Optional<RoomSchedule> findLastScheduleByRoomId(
            @Param("roomId") Long roomId,
            @Param("bookingId") Long bookingId
    );

    List<RoomSchedule> findByBookingId(Long bookingId);

    @Query("""
                SELECT rs FROM RoomSchedule rs
                WHERE rs.roomId = :roomId
                AND CURRENT_TIMESTAMP BETWEEN rs.startTime AND rs.endTime
            """)
    Optional<RoomSchedule> findCurrentByRoomId(Long roomId);

    @Query(value = """
        SELECT TOP 1 rt.name
        FROM Bookings b
        JOIN RoomSchedules rs ON rs.booking_id = b.id
        JOIN Rooms r ON rs.room_id = r.id
        JOIN RoomTypes rt ON r.type_id = rt.id
        GROUP BY rt.name
        ORDER BY COUNT(*) DESC
    """, nativeQuery = true)
    String getTopRoomType();
}
