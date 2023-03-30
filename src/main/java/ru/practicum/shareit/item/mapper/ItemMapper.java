package ru.practicum.shareit.item.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingIdBookerIdDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreationDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Component
public class ItemMapper {

    public ItemCreationDto toDto(Item item) {
        ItemCreationDto itemCreationDto = new ItemCreationDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable()
        );
        if (item.getRequest() != null) {
            itemCreationDto.setRequestId(item.getRequest().getId());
        }
        return itemCreationDto;
    }

    public Item toItem(ItemCreationDto itemDto, User user) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                user,
                null
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
}
