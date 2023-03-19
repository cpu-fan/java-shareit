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

    @Query("select b " +
            "from Booking b " +
            "where b.item.owner.id = ?1 " +
            "order by b.start desc")
    List<Booking> findByOwnerId(long ownerId);

    List<Booking> findByItemIdAndBookerIdAndStatusAndEndBefore(long itemId, long bookerId,
                                                         BookingStatus status, LocalDateTime end);
}
