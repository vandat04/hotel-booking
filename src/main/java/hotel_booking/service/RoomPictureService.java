package hotel_booking.service;

import hotel_booking.dto.response.CloudinaryResponse;
import hotel_booking.entity.RoomPicture;
import hotel_booking.entity.RoomType;
import hotel_booking.repository.RoomPictureRepository;
import hotel_booking.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomPictureService {

    private final RoomPictureRepository roomPictureRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final CloudinaryService cloudinaryService;

    private final String UPLOAD_DIR = "uploads/";

    public List<RoomPicture> uploadImages(Integer roomTypeId, MultipartFile[] files) {

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("RoomType not found"));

        List<RoomPicture> result = new ArrayList<>();

        for (MultipartFile file : files) {

            if (file.isEmpty()) continue;

            CloudinaryResponse res = cloudinaryService.uploadFile(file);

            RoomPicture picture = new RoomPicture();
            picture.setImageUrl(res.getUrl());
            picture.setPublicId(res.getPublicId());
            picture.setRoomType(roomType);

            result.add(roomPictureRepository.save(picture));
        }

        return result;
    }

    public void deleteImage(Integer id) {

        RoomPicture picture = roomPictureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        // xoá cloud
        cloudinaryService.deleteFile(picture.getPublicId());

        // xoá DB
        roomPictureRepository.delete(picture);
    }

    public List<RoomPicture> getImagesByRoomType(Integer roomTypeId) {
        return roomPictureRepository.findByRoomTypeId(roomTypeId);
    }

}
