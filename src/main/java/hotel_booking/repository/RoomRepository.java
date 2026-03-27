package hotel_booking.repository;

import hotel_booking.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByRoomNumber(String roomNumber);

    boolean existsByRoomNumber(String roomNumber);

    Page<Room> findByTypeId(Integer typeId, Pageable pageable);

    Page<Room> findByStatus(String status, Pageable pageable);

    Page<Room> findByTypeIdAndStatus(Integer typeId, String status, Pageable pageable);

    Page<Room> findByRoomNumberContainingIgnoreCase(String roomNumber, Pageable pageable);

    Page<Room> findByRoomNumberContainingIgnoreCaseAndTypeId(String roomNumber, Integer typeId, Pageable pageable);

    Page<Room> findByRoomNumberContainingIgnoreCaseAndStatus(String roomNumber, String status, Pageable pageable);

    Page<Room> findByRoomNumberContainingIgnoreCaseAndTypeIdAndStatus(
            String roomNumber, Integer typeId, String status, Pageable pageable);

    long countByTypeId(Long typeId);

    @Query("""
        SELECT r.status, COUNT(r)
        FROM Room r
        WHERE r.typeId = :typeId
        GROUP BY r.status
    """)
    List<Object[]> countRoomStatusByTypeId(Long typeId);

    List<Room> findByTypeIdAndStatus(Long typeId, String status);

    @Query("""
    SELECT r
    FROM Room r
    WHERE r.typeId = :typeId
    AND r.status = 'AVAILABLE'
    AND r.id NOT IN (
        SELECT rs.roomId
        FROM RoomSchedule rs
        WHERE rs.status IN ('BOOKED', 'OCCUPIED')
        AND rs.startTime < :checkOut
        AND rs.endTime > :checkIn
    )
    ORDER BY r.id ASC
    """)
    List<Room> findAvailableRoomsForBooking(
            Long typeId,
            LocalDateTime checkIn,
            LocalDateTime checkOut
    );

    List<Room> findByTypeId(Long typeId);
}