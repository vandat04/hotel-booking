package hotel_booking.repository;

import hotel_booking.entity.RoomItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomItemRepository extends JpaRepository<RoomItem, Long> {

    List<RoomItem> findByRoomTypeId(Long roomTypeId);

    Page<RoomItem> findByRoomTypeId(Integer roomTypeId, Pageable pageable);

    Page<RoomItem> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<RoomItem> findByRoomTypeIdAndNameContainingIgnoreCase(
            Integer roomTypeId,
            String name,
            Pageable pageable
    );

    Optional<RoomItem> findById(Integer id);

    Page<RoomItem> findByStatus(Integer status, Pageable pageable);

    Page<RoomItem> findByRoomTypeIdAndStatus(Integer roomTypeId, Integer status, Pageable pageable);

    Page<RoomItem> findByNameContainingIgnoreCaseAndStatus(String name, Integer status, Pageable pageable);

    Page<RoomItem> findByRoomTypeIdAndNameContainingIgnoreCaseAndStatus(
            Integer roomTypeId,
            String name,
            Integer status,
            Pageable pageable
    );

    Optional<RoomItem> findByIdAndStatus(Integer id, Integer status);

    List<RoomItem> findByRoomTypeIdAndStatus(Long roomTypeId, int status);

}
