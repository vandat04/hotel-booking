package hotel_booking.repository;

import hotel_booking.entity.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    boolean existsByName(String name);
    Page<RoomType> findByStatus(Integer status, Pageable pageable);

    Page<RoomType> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<RoomType> findByNameContainingIgnoreCaseAndStatus(
            String name,
            Integer status,
            Pageable pageable
    );

    Optional<RoomType> findById(Integer id);

    boolean existsByIdAndStatus(Long id, Integer status);

    Page<RoomType> findByPriceDayBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

}