package ru.practicum.shareit.item.dao.inmemory;

import ru.practicum.shareit.item.model.Item;

import java.util.Map;

public interface ItemRepositoryImMemory {

    Map<Long, Item> findAll();

    Item findById(long id);

    Item create(Item item);

    Item update(Item item);

    Map<Long, Item> search(String text);
}
