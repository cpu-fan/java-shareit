package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {

    Booking createBooking(Booking booking);

    Booking getBookingById(long bookingId);

    Booking considerationOfRequest(Booking booking, boolean approved);

    List<Booking> getAllBookingsByUser(List<Booking> bookings, BookingState state);
}
