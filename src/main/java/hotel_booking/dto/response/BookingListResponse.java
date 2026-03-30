package hotel_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingListResponse {
    private List<BookingDTO> listData;
    private long totalRecords;
    private int currentPage;
    private int pageSize;
}
