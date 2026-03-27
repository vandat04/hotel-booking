package hotel_booking.repository;

import hotel_booking.entity.RoomPicture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomPictureRepository extends JpaRepository<RoomPicture, Integer> {

    List<RoomPicture> findByRoomTypeId(Integer roomTypeId);
}
