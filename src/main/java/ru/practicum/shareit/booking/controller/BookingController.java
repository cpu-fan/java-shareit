package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final ItemRepository itemRepository;

    private final BookingService bookingService;

    private final BookingRepository bookingRepository;

    private final UserService userService;

    private final Mapper mapper;

    private static final String HEADER_NAME = "X-Sharer-User-Id";

    @PostMapping
    public BookingDto createBooking(@RequestHeader(HEADER_NAME) long userId,
                                    @Valid @RequestBody BookingCreationDto bookingDto) {
        // проверка дат
        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            String message = "Окончание бронирования не может быть раньше его начала";
            log.error(message);
            throw new ValidationException(message);
        }

        // проверка пользователя и вещи
        User user = userService.getUserById(userId);
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> {
                    String message = "Вещь с id = " + bookingDto.getItemId() + " не найдена";
                    log.error(message);
                    throw new NotFoundException(message);
                });
//        Item item = itemService.getItemById(bookingDto.getItemId());

        if (item.getOwner().getId() == userId) {
            String message = "Владельцу запрещено бронировать свои же вещи";
            log.error(message);
            throw new NotFoundException(message);
        }

        if (!item.getAvailable()) {
            String message = "Вещь itemId = " + item.getId() + " недоступна для бронирования";
            log.error(message);
            throw new ValidationException(message);
        }

        Booking booking = mapper.toBooking(bookingDto, user, item);
        booking = bookingService.createBooking(booking);
        return mapper.toDto(booking);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto considerationOfRequest(@RequestHeader(HEADER_NAME) long userId,
                                             @PathVariable long bookingId,
                                             @RequestParam boolean approved) {
        Booking booking = bookingService.getBookingById(bookingId);
        long itemOwnerId = booking.getItem().getOwner().getId();
        if (userId != itemOwnerId) {
            String message = "У пользователя userId = " + userId + " не найдено бронирования bookingId = " + bookingId;
            log.error(message);
            throw new NotFoundException(message);
        }
        return mapper.toDto(bookingService.considerationOfRequest(booking, approved));
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader(HEADER_NAME) long userId,
                                     @PathVariable long bookingId) {
        Booking booking = bookingService.getBookingById(bookingId);
        long bookerId = booking.getBooker().getId();
        long ownerId = booking.getItem().getOwner().getId();
        if (!(userId == bookerId || userId == ownerId)) {
            String message = "У пользователя userId = " + userId + " не найдено бронирования bookingId = " + bookingId;
            log.error(message);
            throw new NotFoundException(message);
        }
        return mapper.toDto(booking);
    }

    @GetMapping
    public List<BookingDto> getAllBookingsByUser(@RequestHeader(HEADER_NAME) long userId,
                                                 @RequestParam(required = false) BookingState state) {
        userService.getUserById(userId); // если такого юзера нет, то 404
        List<Booking> bookings = bookingRepository.findByBookerId(userId);
        return bookingService.getAllBookingsByUser(bookings, state)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllBookingsByOwner(@RequestHeader(HEADER_NAME) long userId,
                                                  @RequestParam(required = false) BookingState state) {
        userService.getUserById(userId);
        List<Booking> bookings = bookingRepository.findByOwnerId(userId);
        return bookingService.getAllBookingsByUser(bookings, state)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
