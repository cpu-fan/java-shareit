package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.dto.BookingIdBookerIdDto;

import java.util.List;

@Data
@AllArgsConstructor
@FieldDefaults(makeFinal = false)
public class ItemDto {

    long id;

    String name;

    String description;

    Boolean available;

    BookingIdBookerIdDto lastBooking;

    BookingIdBookerIdDto nextBooking;

    List<CommentDto> comments;
}
