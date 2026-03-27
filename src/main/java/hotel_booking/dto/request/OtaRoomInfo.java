package hotel_booking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtaRoomInfo {


    private Integer quantity; // số lượng phòng loại này
    private BigDecimal price; // giá thuê / ngày hoặc / giờ
}