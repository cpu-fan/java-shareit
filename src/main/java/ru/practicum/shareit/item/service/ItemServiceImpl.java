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
import ru.practicum.shareit.item.dto.CommentTextDto;
import ru.practicum.shareit.item.dto.ItemCreationDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final BookingRepository bookingRepository;

    private final UserService userService;

    private final CommentRepository commentRepository;

    private final Mapper mapper;

    private final UserRepository userRepository;

    @Override
    public List<ItemDto> getOwnerItems(long userId) {
        log.info("Запрошен список вещей пользователя userId = " + userId);

        List<Item> items = itemRepository.findByOwnerId(userId);

        List<Comment> comments = commentRepository.findByItemsId(items.stream()
                .map(Item::getId)
                .collect(Collectors.toList()));

        return items.stream()
                .map(i -> getItemWithBookings(i, comments.stream()
                        .filter(c -> c.getItem().getId() == i.getId())
                        .map(mapper::toDto)
                        .collect(Collectors.toList())))
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
    public ItemCreationDto createItem(long userId, ItemCreationDto itemDto) {
        User user = userService.getUserById(userId);
        Item item = mapper.toItem(user, itemDto);
        item = itemRepository.save(item);
        log.info("Добавлена новая вещь " + item);
        return mapper.toDto(item);
    }

    @Override
    public ItemCreationDto updateItem(long userId, long itemId, ItemCreationDto itemDto) {
        User user = userService.getUserById(userId);
        Item item = mapper.toItem(user, itemDto);

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
        item = itemRepository.save(updatingItem.get());
        return mapper.toDto(item);
    }

    @Override
    public List<ItemCreationDto> searchItem(String text) {
        if (text.isBlank()) {
            log.info("Для поиска передана пустая строка");
            return Collections.emptyList();
        }
        log.info("Поиск вещи по запросу: \"" + text + "\"");
        List<Item> items = itemRepository.search("%" + text + "%");
        return items.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(long userId,  long itemId, CommentTextDto commentDto) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            String message = "Вещь itemId = " + itemId + " не найдена";
            log.error(message);
            throw new NotFoundException(message);
        });
        User author = userRepository.findById(userId).orElseThrow(() -> {
            String message = "Пользователь userId = " + itemId + " не найден";
            log.error(message);
            throw new NotFoundException(message);
        });

        Comment newComment = mapper.toComment(commentDto, item, author);

        List<Booking> bookings = bookingRepository.findByItemIdAndBookerIdAndStatusAndEndBefore(itemId, userId,
                BookingStatus.APPROVED, LocalDateTime.now());
        if (bookings.isEmpty()) {
            String message = "Невозможно добавить отзыв, т.к. пользователь userId = " + userId
                    + " ни разу не брал в аренду вещь itemId = " + itemId;
            log.error(message);
            throw new ValidationException(message);
        }

        newComment =  commentRepository.save(newComment);
        return mapper.toDto(newComment);
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
