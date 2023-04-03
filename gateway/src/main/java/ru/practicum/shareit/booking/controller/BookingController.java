package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingState;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/bookings")
@Validated
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private static final String HEADER_NAME = "X-Sharer-User-Id";

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader(HEADER_NAME) long userId,
                                                @Valid @RequestBody BookingCreationDto bookingDto) {
        log.info("Пользователь userId = {} запрашивает бронирование вещи itemId = {}", userId, bookingDto.getItemId());
        return bookingClient.createBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> considerationOfRequest(@RequestHeader(HEADER_NAME) long userId,
                                             @PathVariable long bookingId,
                                             @RequestParam boolean approved) {
        log.info("Пользователь userId = {} устанавливает статус approved = {} бронированию bookingId = {}",
                userId, approved, bookingId);
        return bookingClient.considerationOfRequest(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader(HEADER_NAME) long userId,
                                     @PathVariable long bookingId) {
        log.info("Пользователь userId = {} запрашивает бронирование bookingId = {}", userId, bookingId);
        return bookingClient.getBookingById(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllBookingsByUser(@RequestHeader(HEADER_NAME) long userId,
                                                 @RequestParam(defaultValue = "ALL") BookingState state,
                                                 @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                 @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Запрос state = {} бронирований пользователем userId = {}. Параметры: from = {}, size = {}",
                state, userId, from, size);
        return bookingClient.getAllBookingsByUser(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllBookingsByOwner(@RequestHeader(HEADER_NAME) long userId,
                                                  @RequestParam(defaultValue = "ALL") BookingState state,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                  @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Запрос state = {} бронирований пользователем userId = {}. Параметры: from = {}, size = {}",
                state, userId, from, size);
        return bookingClient.getAllBookingsByOwner(userId, state, from, size);
    }
}
