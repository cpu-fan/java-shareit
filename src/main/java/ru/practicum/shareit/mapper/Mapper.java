package ru.practicum.shareit.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingIdBookerIdDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserIdDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class Mapper {

    public ItemCreationDto toDto(Item item) {
        // Можно ли по code style большие конструкторы таким образом собирать?
        // Выглядит вроде нагляднее... (решил попробовать в этом проекте без @Builder обойтись)
        return new ItemCreationDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable()
        );
    }

    public Item toItem(User user, ItemCreationDto itemDto) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                user, null
        );
    }

    public ItemDto toDto(Item item,
                         BookingIdBookerIdDto lastBooking,
                         BookingIdBookerIdDto nextBooking,
                         List<CommentDto> comments) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                lastBooking,
                nextBooking,
                comments
        );
    }

    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public User toUser(UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getName(),
                userDto.getEmail()
        );
    }

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

    public Comment toComment(CommentTextDto comment, Item item, User author) {
        return new Comment(
                0,
                comment.getText(),
                item,
                author,
                LocalDateTime.now()
        );
    }

    public CommentDto toDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }

    public Map<String, Object> toMap(Object shareItObject) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> objectFields = mapper.convertValue(shareItObject, new TypeReference<>() {});
        return objectFields.entrySet()
                .stream()
                .filter(k -> k.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
