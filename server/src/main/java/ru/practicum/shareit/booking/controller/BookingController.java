package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String HEADER_NAME = "X-Sharer-User-Id";

    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(@RequestHeader(HEADER_NAME) long userId,
                                    @RequestBody BookingCreationDto bookingDto) {
        return bookingService.createBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto considerationOfRequest(@RequestHeader(HEADER_NAME) long userId,
                                             @PathVariable long bookingId,
                                             @RequestParam boolean approved) {
        return bookingService.considerationOfRequest(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader(HEADER_NAME) long userId,
                                     @PathVariable long bookingId) {
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getAllBookingsByUser(@RequestHeader(HEADER_NAME) long userId,
                                                 @RequestParam BookingState state,
                                                 @RequestParam int from,
                                                 @RequestParam int size) {
        return bookingService.getAllBookingsByUser(userId, state, from, size, false);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllBookingsByOwner(@RequestHeader(HEADER_NAME) long userId,
                                                  @RequestParam BookingState state,
                                                  @RequestParam int from,
                                                  @RequestParam int size) {
        return bookingService.getAllBookingsByUser(userId, state, from, size, true);
    }
}
