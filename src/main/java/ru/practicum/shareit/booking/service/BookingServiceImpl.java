package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    private final ItemRepository itemRepository;

    private final UserService userService;

    private final Mapper mapper;

    @Override
    public BookingDto createBooking(long userId, BookingCreationDto bookingDto) {
        User user = userService.getUserById(userId);
        Item item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(() -> {
            String message = "Вещь itemId = " + bookingDto.getItemId() + " не найдена";
            log.error(message);
            throw new NotFoundException(message);
        });

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
        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            String message = "Окончание бронирования не может быть раньше его начала";
            log.error(message);
            throw new ValidationException(message);
        }

        // Проверка, есть ли уже бронирование данной вещи
        checkItemBookings(bookingDto);

        // Устанавливаю статус и сохраняю
        Booking booking = mapper.toBooking(bookingDto, user, item);
        booking.setStatus(BookingStatus.WAITING);
        booking = bookingRepository.save(booking);
        log.info("Добавлен запрос на бронирование вещи itemId = " + booking.getItem().getId()
                + " пользователем userId = " + userId);

        return mapper.toDto(booking);
    }

    @Override
    public BookingDto considerationOfRequest(long userId, long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
            String message = "Бронирование bookingId = " + bookingId + " не найдена";
            log.error(message);
            throw new NotFoundException(message);
        });

        long itemOwnerId = booking.getItem().getOwner().getId();
        if (userId != itemOwnerId) {
            String message = "У пользователя userId = " + userId + " не найдено бронирования bookingId = " + bookingId;
            log.error(message);
            throw new NotFoundException(message);
        }

        if (booking.getStatus().equals(BookingStatus.APPROVED)
                || booking.getStatus().equals(BookingStatus.REJECTED)) {
            String message = "Бронирование bookingId" + bookingId + " уже было рассмотрено";
            log.error(message);
            throw new ValidationException(message);
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        bookingRepository.save(booking);
        log.info("Бронирование bookingId = " + booking.getId() + " переведено в статус " + booking.getStatus());
        return mapper.toDto(booking);
    }

    @Override
    public BookingDto getBookingById(long userId, long bookingId) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) {
            String message = "Бронирование bookingId = " + bookingId + " не найдено";
            log.error(message);
            throw new NotFoundException(message);
        }

        long bookerId = booking.get().getBooker().getId();
        long ownerId = booking.get().getItem().getOwner().getId();
        if (!(userId == bookerId || userId == ownerId)) {
            String message = "У пользователя userId = " + userId + " не найдено бронирования bookingId = " + bookingId;
            log.error(message);
            throw new NotFoundException(message);
        }

        log.info("Запрошено бронирование bookingId = " + bookingId);
        return mapper.toDto(booking.get());
    }

    @Override
    public List<BookingDto> getAllBookingsByUser(long userId, BookingState state, boolean isOwner) {
        userService.getUserById(userId); // если такого юзера нет, то 404
        List<Booking> bookings;

        if (isOwner) {
            bookings = bookingRepository.findByOwnerId(userId);
        } else {
            bookings = bookingRepository.findByBookerId(userId);
        }

        if (state == null) {
            state = BookingState.ALL;
        }

        switch (state) {
            case WAITING:
            case REJECTED:
                String strState = state.toString();
                Predicate<Booking> statusPredicate = b -> b.getStatus().toString().equals(strState);
                return getFilteredAndSortBookings(bookings, statusPredicate);

            case PAST:
                Predicate<Booking> pastPredicate = b -> b.getEnd().isBefore(LocalDateTime.now());
                return getFilteredAndSortBookings(bookings, pastPredicate);

            case CURRENT:
                Predicate<Booking> currentPredicate = b -> b.getStart().isBefore(LocalDateTime.now())
                        && b.getEnd().isAfter(LocalDateTime.now());
                return getFilteredAndSortBookings(bookings, currentPredicate);

            case FUTURE:
                Predicate<Booking> futurePredicate = b -> b.getStart().isAfter(LocalDateTime.now());
                return getFilteredAndSortBookings(bookings, futurePredicate);

            case ALL:
                return bookings.stream()
                        .sorted(Comparator.comparing(Booking::getStart).reversed())
                        .map(mapper::toDto)
                        .collect(Collectors.toList());

            default:
                String message = "Unknown state: " + state;
                log.error(message);
                throw new ValidationException(message);
        }
    }

    private void checkItemBookings(BookingCreationDto booking) {
        long itemId = booking.getItemId();
        List<Booking> existingBookings = bookingRepository.findByItemId(itemId);

        // Если бронирование имеется даты пересекаются, то выбрасываем ошибку
        if (existingBookings != null) {
            LocalDateTime startNewBooking = booking.getStart();
            LocalDateTime endNewBooking = booking.getEnd();

            for (Booking existingBooking : existingBookings) {
                LocalDateTime startExistBooking = existingBooking.getStart();
                LocalDateTime endExistBooking = existingBooking.getEnd();

                if (isOverlapping(startNewBooking, endNewBooking, startExistBooking, endExistBooking)) {
                    String message = "Бронирование вещи itemId = " + itemId + " на выбранный период " +
                            "пересекается с другим бронированием этой вещи";
                    log.error(message);
                    throw new ConflictException(message);
                }
            }
        }
    }

    private boolean isOverlapping(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    List<BookingDto> getFilteredAndSortBookings(List<Booking> list, Predicate<Booking> predicate) {
        return list.stream()
                .filter(predicate)
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
