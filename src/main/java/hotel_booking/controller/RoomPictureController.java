package hotel_booking.controller;

import hotel_booking.entity.RoomPicture;
import hotel_booking.service.RoomPictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/room-pictures")
@RequiredArgsConstructor
public class RoomPictureController {
    private final RoomPictureService roomPictureService;

    @PostMapping("/upload")
    public ResponseEntity<List<RoomPicture>> uploadImages(
            @RequestParam Integer roomTypeId,
            @RequestParam("files") MultipartFile[] files
    ) {
        List<RoomPicture> uploaded = roomPictureService.uploadImages(roomTypeId, files);
        return ResponseEntity.ok(uploaded);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteImage(@PathVariable Integer id) {
        roomPictureService.deleteImage(id);
        return ResponseEntity.ok("Deleted successfully");
    }

    @GetMapping("/room/{roomTypeId}")
    public ResponseEntity<List<RoomPicture>> getImagesByRoomType(@PathVariable Integer roomTypeId) {
        List<RoomPicture> pictures = roomPictureService.getImagesByRoomType(roomTypeId);
        return ResponseEntity.ok(pictures);
    }
}
