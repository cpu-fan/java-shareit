package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.Map;

public interface ItemRepository {

    Map<Long, Item> findAll();

    Item findById(long id);

    Item create(Item item);

    Item update(Item item);

    Map<Long, Item> search(String text);
}
