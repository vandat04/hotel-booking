package hotel_booking.repository;

import hotel_booking.entity.CheckList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CheckListRepository extends JpaRepository<CheckList, Long> {

    List<CheckList> findByCleaningTaskId(Long taskId);
    boolean existsByItemId(Integer itemId);
}
