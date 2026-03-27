package hotel_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class RoomKeyResponse {
    private Long id;
    private Long roomId;
    private String qrCode;
    private String numberCode;
    private Integer status;
    private LocalDateTime expiredAt;
}
