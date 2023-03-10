package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    List<ItemDto> getOwnerItems(long userId);

    ItemDto getItemById(long userId, long itemId);

    Item createItem(Item item);

    Item updateItem(long userId, long itemId, Item item);

    List<Item> searchItem(String text);

    Comment addComment(Comment comment, long itemId, long authorId);
}
