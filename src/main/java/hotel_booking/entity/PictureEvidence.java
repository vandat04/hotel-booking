package hotel_booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PictureEvidence")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PictureEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "checklist_id")
    private Long checklistId;

    private String url;
}
