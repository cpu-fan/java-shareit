package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByItemId(long itemId);

    List<Booking> findByBookerId(long bookerId);

    @Query(value = "select b.id, b.start_date, b.end_date, b.item_id, b.booker_id, b.status " +
            "from bookings b " +
            "left join items i on i.id = b.item_id " +
            "where i.owner_id = ?1 " +
            "order by start_date desc", nativeQuery = true)
    List<Booking> findByOwnerId(long ownerId);

    List<Booking> findByItemIdAndBookerIdAndStatusAndEndBefore(long itemId, long bookerId,
                                                         BookingStatus status, LocalDateTime end);
}
