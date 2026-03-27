package hotel_booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Salaries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "salary_month", "salary_year"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "salary_month")
    private Integer salaryMonth;
    @Column(name = "salary_year")
    private Integer salaryYear;
    @Column(name = "total_salary")
    private BigDecimal totalSalary;

    private Integer status;
    @Column(name = "attendance")
    private Integer attendance;
}
