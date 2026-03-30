package hotel_booking.specification;

import hotel_booking.entity.Booking;
import hotel_booking.entity.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingSpecification {

    public static Specification<Booking> filter(
            String status,
            String source,
            String channel,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add((Predicate) cb.equal(root.get("status"), status));
            }

            if (source != null) {
                predicates.add((Predicate) cb.equal(root.get("source"), source));
            }

            if (channel != null) {
                predicates.add((Predicate) cb.equal(root.get("channel"), channel));
            }

            if (fromDate != null && toDate != null) {
                predicates.add((Predicate) cb.between(root.get("createdAt"), fromDate, toDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Booking> search(String keyword) {
        return (root, query, cb) -> {

            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }

            String like = "%" + keyword.trim() + "%";

            // ✅ ID
            Predicate byId;
            if (keyword.matches("\\d+")) {
                byId = cb.equal(root.get("id"), Integer.parseInt(keyword));
            } else {
                byId = cb.disjunction();
            }

            // ✅ WEBSITE → Users
            Subquery<Long> sub = query.subquery(Long.class);
            Root<User> u = sub.from(User.class);

            sub.select(u.get("id"))
                    .where(
                            cb.or(
                                    cb.like(u.get("fullName"), like),
                                    cb.like(u.get("phone"), like),
                                    cb.like(u.get("email"), like)
                            )
                    );

            Predicate websiteCondition = cb.and(
                    cb.equal(root.get("channel"), "WEBSITE"),
                    root.get("userId").in(sub)
            );

            // ✅ OTHER → note
            Predicate otherCondition = cb.and(
                    cb.notEqual(root.get("channel"), "WEBSITE"),
                    cb.like(cb.coalesce(root.get("note"), ""), like)
            );

            return cb.or(byId, websiteCondition, otherCondition);
        };
    }


}
