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
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public List<Item> getOwnerItems(long userId) {
        return itemRepository.findAll().values()
                .stream()
                .filter(i -> i.getOwner().getId() == userId)
                .collect(Collectors.toList());
    }

    @Override
    public Item getItemById(long itemId) {
        Item item = itemRepository.findById(itemId);
        if (item == null) {
            String message = "Вещь с id = " + itemId + " не найдена";
            log.error(message);
            throw new NotFoundException(message);
        }
        return item;
    }

    @Override
    public Item createItem(Item item) {
        item = itemRepository.create(item);
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
        return updatingItem;
    }

    @Override
    public List<Item> searchItem(String text) {
        if (text.isBlank()) {
            log.info("Для поиска передана пустая строка");
            return Collections.emptyList();
        }

        Pattern pattern = Pattern.compile(text, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Predicate<Item> namePredicate = n -> pattern.matcher(n.getName()).find();
        Predicate<Item> descriptionPredicate = n -> pattern.matcher(n.getDescription()).find();

        log.info("Запрошен поиск по строке: " + text.toLowerCase());
        return itemRepository.findAll().values()
                .stream()
                .filter(namePredicate.or(descriptionPredicate))
                .filter(Item::getAvailable)
                .collect(Collectors.toList());
    }
}
