package hotel_booking.repository;

import hotel_booking.entity.PictureEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PictureEvidenceRepository extends JpaRepository<PictureEvidence, Long> {

    List<PictureEvidence> findByChecklistId(Long checklistId);
}
