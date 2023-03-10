package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;

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

    @Override
    public Booking createBooking(Booking booking) {
        // Сначала проверяю, есть ли уже бронирование данной вещи
        long itemId = booking.getItem().getId();
        List<Booking> existingBookings = bookingRepository.findByItemId(itemId);
        // И если есть и даты пересекаются, то выбрасываем ошибку
        if (existingBookings != null) {
            for (Booking existingBooking : existingBookings) {
                LocalDateTime startNewBooking = booking.getStart();
                LocalDateTime endNewBooking = booking.getEnd();
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
        // Устанавливаю статус и сохраняю
        booking.setStatus(BookingStatus.WAITING);
        booking = bookingRepository.save(booking);
        log.info("Добавлен запрос на бронирование вещи itemId = " + booking.getItem().getId()
                + " пользователем userId = " + booking.getBooker().getId());
        return booking;
    }

    @Override
    public Booking getBookingById(long bookingId) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) {
            String message = "Бронирование bookingId = " + bookingId + " не найдено";
            log.error(message);
            throw new NotFoundException(message);
        }
        log.info("Запрошено бронирование bookingId = " + bookingId);
        return booking.get();
    }

    @Override
    public Booking considerationOfRequest(Booking booking, boolean approved) {
        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            String message = "Бронирование bookingId" + booking.getId() + " уже одобрено";
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
        return booking;
    }

    @Override
    public List<Booking> getAllBookingsByUser(List<Booking> bookings, BookingState state) {
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
                        .collect(Collectors.toList());

            default:
                String message = "Unknown state: " + state;
                log.error(message);
                throw new ValidationException(message);
        }
    }

    private boolean isOverlapping(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    List<Booking> getFilteredAndSortBookings(List<Booking> list, Predicate<Booking> predicate) {
        return list.stream()
                .filter(predicate)
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
    }
}
