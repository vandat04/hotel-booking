package hotel_booking.repository;

import hotel_booking.entity.CleaningTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CleaningTaskRepository extends JpaRepository<CleaningTask, Long> {

    List<CleaningTask> findByRoomId(Long roomId);

    List<CleaningTask> findByCleanerId(Long cleanerId);
}
