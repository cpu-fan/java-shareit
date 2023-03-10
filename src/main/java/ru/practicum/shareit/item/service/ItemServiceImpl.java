package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingIdBookerIdDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.mapper.Mapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final BookingRepository bookingRepository;

    private final CommentRepository commentRepository;

    private final Mapper mapper;

    @Override
    public List<ItemDto> getOwnerItems(long userId) {
        log.info("Запрошен список вещей пользователя userId = " + userId);
        List<Item> items = itemRepository.findAll()
                .stream()
                .filter(i -> i.getOwner().getId() == userId)
                .collect(Collectors.toList());

        List<ItemDto> list = new ArrayList<>();
        for (Item item : items) {
            List<CommentDto> comments = commentRepository.findByItemId(item.getId()).stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());
            list.add(getItemWithBookings(item, comments));
        }

        return list.stream()
                .sorted(Comparator.comparing(ItemDto::getId))
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(long userId, long itemId) {
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isEmpty()) {
            String message = "Вещь с itemId = " + itemId + " не найдена";
            log.error(message);
            throw new NotFoundException(message);
        }

        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        log.info("Запрошена вещь с itemId = " + itemId);

        if (item.get().getOwner().getId() == userId) {
            return getItemWithBookings(item.get(), comments);
        }
        return mapper.toDto(item.get(), null, null, comments);
    }

    @Override
    public Item createItem(Item item) {
        item = itemRepository.save(item);
        log.info("Добавлена новая вещь " + item);
        return item;
    }

    @Override
    public Item updateItem(long userId, long itemId, Item item) {
        Optional<Item> updatingItem = itemRepository.findById(itemId);
        if (updatingItem.isEmpty()) {
            String message = "Вещь с itemId = " + itemId + " не найдена";
            log.error(message);
            throw new NotFoundException(message);
        }

        long itemOwnerId = updatingItem.get().getOwner().getId();
        if (userId != itemOwnerId) {
            log.error("Пользователь id = " + userId + " не имеет доступа к редактированию вещи id = " + itemId);
            throw new ForbiddenException("Access Denied");
        }

        // Конвертирую Item в Map и отбираю только те поля-ключи, значение у которых != null
        Mapper mapper = new Mapper();
        Map<String, Object> itemFields = mapper.toMap(item);

        // Далее обновляю необходимые значения полей у существующего объекта
        if (itemFields.containsKey("name")) {
            updatingItem.get().setName(item.getName());
        }
        if (itemFields.containsKey("description")) {
            updatingItem.get().setDescription(item.getDescription());
        }
        if (itemFields.containsKey("available")) {
            updatingItem.get().setAvailable(item.getAvailable());
        }

        log.info("Обновлена информация о вещи " + updatingItem);
        return itemRepository.save(updatingItem.get());
    }

    @Override
    public List<Item> searchItem(String text) {
        if (text.isBlank()) {
            log.info("Для поиска передана пустая строка");
            return Collections.emptyList();
        }
        log.info("Поиск вещи по запросу: \"" + text + "\"");
        return itemRepository.search("%" + text + "%");
    }

    @Override
    public Comment addComment(Comment comment, long itemId, long authorId) {
        List<Booking> bookings = bookingRepository.findByItemIdAndBookerIdAndStatusAndEndBefore(itemId, authorId,
                BookingStatus.APPROVED, LocalDateTime.now());
        if (bookings.isEmpty()) {
            String message = "Невозможно добавить отзыв, т.к. пользователь userId = " + authorId
                    + " ни разу не брал в аренду вещь itemId = " + itemId;
            log.error(message);
            throw new ValidationException(message);
        }
        return commentRepository.save(comment);
    }

    private ItemDto getItemWithBookings(Item item, List<CommentDto> comments) {
        List<Booking> bookings = bookingRepository.findByItemId(item.getId());

        BookingIdBookerIdDto lastBooking = bookings
                .stream()
                .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .map(mapper::toBookingIdBookerIdDto)
                .findFirst()
                .orElse(null);

        BookingIdBookerIdDto nextBooking = bookings
                .stream()
                .filter(b -> b.getStart().isAfter(LocalDateTime.now())
                        && b.getStatus() != BookingStatus.REJECTED)
                .sorted(Comparator.comparing(Booking::getStart))
                .map(mapper::toBookingIdBookerIdDto)
                .findFirst()
                .orElse(null);

        return mapper.toDto(item, lastBooking, nextBooking, comments);
    }
}
