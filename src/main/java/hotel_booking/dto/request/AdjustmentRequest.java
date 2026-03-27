package hotel_booking.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdjustmentRequest {

    private Long recordId; // record cần sửa/xoá
    private BigDecimal amount; // số tiền điều chỉnh (âm/dương)
    private String note;
}
