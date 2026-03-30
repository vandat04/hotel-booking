package hotel_booking.repository;

import hotel_booking.entity.RoomKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface RoomKeyRepository extends JpaRepository<RoomKey, Long> {
    void deleteByBookingId(Long bookingId);

    List<RoomKey> findByBookingId(Long bookingId);

    List<RoomKey> findByRoomIdIn(Set<Long> roomIds);

}
