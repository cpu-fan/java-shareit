package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentTextDto;
import ru.practicum.shareit.item.dto.ItemCreationDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    List<ItemDto> getOwnerItems(long userId, int from, int size);

    ItemDto getItemById(long userId, long itemId);

    ItemCreationDto createItem(long userId, ItemCreationDto itemDto);

    ItemCreationDto updateItem(long userId, long itemId, ItemCreationDto item);

    List<ItemCreationDto> searchItem(String text, int from, int size);

    CommentDto addComment(long userId,  long itemId, CommentTextDto comment);
}
