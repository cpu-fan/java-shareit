package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    List<Item> getOwnerItems(long userId);

    Item getItemById(long itemId);

    Item createItem(Item item);

    Item updateItem(long itemId, Item item);

    List<Item> searchItem(String text);
}
