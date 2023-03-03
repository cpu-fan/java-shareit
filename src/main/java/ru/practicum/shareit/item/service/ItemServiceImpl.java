package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.mapper.Mapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public List<Item> getOwnerItems(long userId) {
        return itemRepository.findAll()
                .stream()
                .filter(i -> i.getOwner().getId() == userId)
                .collect(Collectors.toList());
    }

    @Override
    public Item getItemById(long itemId) {
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isEmpty()) {
            String message = "Вещь с id = " + itemId + " не найдена";
            log.error(message);
            throw new NotFoundException(message);
        }
        return item.get();
    }

    @Override
    public Item createItem(Item item) {
        item = itemRepository.save(item);
        log.info("Добавлена новая вещь " + item);
        return item;
    }

    @Override
    public Item updateItem(long itemId, Item item) {
        Item updatingItem = getItemById(itemId);

        // Конвертирую Item в Map и отбираю только те поля-ключи, значение у которых != null
        Mapper mapper = new Mapper();
        Map<String, Object> itemFields = mapper.toMap(item);

        // Далее "точечно" обновляю значения полей у существующего объекта
        if (itemFields.containsKey("name")) {
            updatingItem.setName(item.getName());
        }
        if (itemFields.containsKey("description")) {
            updatingItem.setDescription(item.getDescription());
        }
        if (itemFields.containsKey("available")) {
            updatingItem.setAvailable(item.getAvailable());
        }

        log.info("Обновлена информация о вещи " + updatingItem);
        return itemRepository.save(updatingItem);
    }

    @Override
    public List<Item> searchItem(String text) {
        if (text.isBlank()) {
            log.info("Для поиска передана пустая строка");
            return Collections.emptyList();
        }
        return itemRepository.search("%" + text + "%");
    }
}
