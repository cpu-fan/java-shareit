package ru.practicum.shareit.booking.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingIdBookerIdDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemIdNameDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserIdDto;
import ru.practicum.shareit.user.model.User;

@Component
public class BookingMapper {

    public BookingDto toDto(Booking booking) {
        UserIdDto userIdDto = new UserIdDto(booking.getBooker().getId());
        long itemId = booking.getItem().getId();
        String itemName = booking.getItem().getName();
        ItemIdNameDto itemIdNameDto = new ItemIdNameDto(itemId, itemName);
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                userIdDto,
                itemIdNameDto
        );
    }

    public Booking toBooking(BookingCreationDto bookingDto, User user, Item item) {
        Booking booking = new Booking(
                0,
                bookingDto.getStart(),
                bookingDto.getEnd(),
                null, null, null
        );
        booking.setBooker(user);
        booking.setItem(item);
        return booking;
    }

    public BookingIdBookerIdDto toBookingIdBookerIdDto(Booking booking) {
        return new BookingIdBookerIdDto(
                booking.getId(),
                booking.getBooker().getId()
        );
    }
}
