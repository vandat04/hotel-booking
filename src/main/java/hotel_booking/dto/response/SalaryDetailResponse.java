package hotel_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalaryDetailResponse {

    private SalaryResponse salary;
    private UserProfileResponse user;
}
