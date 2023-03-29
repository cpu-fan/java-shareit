package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final BookingRepository bookingRepository;

    private final CommentRepository commentRepository;

    private final Mapper mapper;

    private final UserService userService;

    private final ItemRequestRepository itemRequestRepository;

    @Override
    public List<ItemDto> getOwnerItems(long userId, int from, int size) {
        log.info("Запрошен список вещей пользователя userId = " + userId);

        List<Item> items = itemRepository.findByOwnerId(userId, PageRequest.of(from / size, size,
                Sort.by("id")));

        Map<Long, List<Comment>> comments = commentRepository.findByItemsId(items.stream()
                .map(Item::getId)
                .collect(Collectors.toList())).stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId(),
                        Collectors.mapping(c -> c, Collectors.toList())));

        return items.stream()
                .map(i -> getItemWithBookings(i, comments.getOrDefault(i.getId(), Collections.emptyList()).stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(long userId, long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            String message = "Вещь с itemId = " + itemId + " не найдена";
            log.error(message);
            throw new NotFoundException(message);
        });

        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        log.info("Запрошена вещь с itemId = " + itemId);

        if (item.getOwner().getId() == userId) {
            return getItemWithBookings(item, comments);
        }
        return mapper.toDto(item, null, null, comments);
    }

    @Override
    public ItemCreationDto createItem(long userId, ItemCreationDto itemDto) {
        long requestId = itemDto.getRequestId();
        User user = mapper.toUser(userService.getUserById(userId));
        Item item = mapper.toItem(itemDto, user);
        if (requestId != 0) {
            setItemRequest(item, requestId);
        }
        item = itemRepository.save(item);
        log.info("Добавлена новая вещь " + item);
        return mapper.toDto(item);
    }

    @Override
    public ItemCreationDto updateItem(long userId, long itemId, ItemCreationDto itemDto) {
        long requestId = itemDto.getRequestId();
        User user = mapper.toUser(userService.getUserById(userId));
        Item item = mapper.toItem(itemDto, user);
        if (requestId != 0) {
            setItemRequest(item, requestId);
        }

        Item updatingItem = itemRepository.findById(itemId).orElseThrow(() -> {
            String message = "Вещь с itemId = " + itemId + " не найдена";
            log.error(message);
            throw new NotFoundException(message);
        });

        long itemOwnerId = updatingItem.getOwner().getId();
        if (userId != itemOwnerId) {
            log.error("Пользователь id = " + userId + " не имеет доступа к редактированию вещи id = " + itemId);
            throw new ForbiddenException("Access Denied");
        }

        // Конвертирую Item в Map и отбираю только те поля-ключи, значение у которых != null
        Map<String, Object> itemFields = mapper.toMap(item);
        // Далее обновляю необходимые значения полей у существующего объекта
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
        item = itemRepository.save(updatingItem);
        return mapper.toDto(item);
    }

    @Override
    public List<ItemCreationDto> searchItem(String text, int from, int size) {
        if (text.isBlank()) {
            log.info("Для поиска передана пустая строка");
            return Collections.emptyList();
        }
        log.info("Поиск вещи по запросу: \"" + text + "\"");
        List<Item> items = itemRepository.search(text, PageRequest.of(from / size, size));
        return items.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(long userId,  long itemId, CommentTextDto commentDto) {
        User author = mapper.toUser(userService.getUserById(userId));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            String message = "Вещь itemId = " + itemId + " не найдена";
            log.error(message);
            throw new NotFoundException(message);
        });

        List<Booking> bookings = bookingRepository.findByItemIdAndBookerIdAndStatusAndEndBefore(itemId, userId,
                BookingStatus.APPROVED, LocalDateTime.now());
        if (bookings.isEmpty()) {
            String message = "Невозможно добавить отзыв, т.к. пользователь userId = " + userId
                    + " ни разу не брал в аренду вещь itemId = " + itemId;
            log.error(message);
            throw new ValidationException(message);
        }

        Comment newComment = mapper.toComment(commentDto, item, author);
        newComment =  commentRepository.save(newComment);
        return mapper.toDto(newComment);
    }

    private ItemDto getItemWithBookings(Item item, List<CommentDto> comments) {
        List<Booking> bookings = bookingRepository.findByItemId(item.getId());

        BookingIdBookerIdDto lastBooking = bookings
                .stream()
                .filter(b -> b.getEnd().isBefore(LocalDateTime.now())
                        || b.getStart().isBefore(LocalDateTime.now()))
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

    private void setItemRequest(Item item, long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(() -> {
            String message = "Запрос requestId " + requestId + " не найден";
            log.error(message);
            throw new NotFoundException(message);
        });
        item.setRequest(itemRequest);
    }
}
