package ru.practicum.shareit.item.dao;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.Map;

@Repository
public class ItemRepositoryInMemImpl implements ItemRepository {

    private final Map<Long, Item> itemMap = new HashMap<>();
    private static long id = 0;

    @Override
    public Map<Long, Item> findAll() {
        return itemMap;
    }

    @Override
    public Item findById(long id) {
        return itemMap.get(id);
    }

    @Override
    public Item create(Item item) {
        item.setId(generateId());
        itemMap.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        long itemId = item.getId();
        itemMap.put(itemId, item);
        return itemMap.get(itemId);
    }

    @Override
    public Map<Long, Item> search(String text) {
        return null;
    }

    private long generateId() {
        return ++id;
    }
}
