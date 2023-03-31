package ru.practicum.shareit.request.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDtoForRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreatedDto;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ItemRequestMapper {

    public ItemDtoForRequest toItemDtoForReq(Item item) {
        return new ItemDtoForRequest(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest().getId()
        );
    }

    public ItemRequest toItemRequest(User requestor, ItemRequestCreationDto itemRequestDto) {
        return new ItemRequest(
                0,
                itemRequestDto.getDescription(),
                requestor,
                LocalDateTime.now()
        );
    }

    public ItemRequestCreatedDto toDto(ItemRequest itemRequest) {
        return new ItemRequestCreatedDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated()
        );
    }

    public ItemRequestDto toItemRequestDto(ItemRequest itemRequest, List<ItemDtoForRequest> items) {
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                items
        );
    }
}
