package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Sql(scripts = "/schema.sql")
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User booker;
    private ItemRequest itemRequest;
    private Booking booking;
    private BookingCreationDto bookingCreationDto;

    @BeforeEach
    void setUp() {
        owner = new User(1, "owner", "email@owner.com");
        User requestor = new User(2, "requestor", "email@requestor.com");
        booker = new User(3, "booker", "email@booker.com");
        User user = new User(4, "user", "email@user.com");
        Item item = new Item(1, "name", "desc", true, owner, itemRequest);
        bookingCreationDto = new BookingCreationDto(item.getId(), LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2));
        booking = new Booking(1, bookingCreationDto.getStart(), bookingCreationDto.getEnd(), item, booker,
                BookingStatus.WAITING);
        itemRequest = new ItemRequest(1, "desc", requestor, LocalDateTime.now());

        userRepository.save(owner);
        userRepository.save(requestor);
        userRepository.save(booker);
        userRepository.save(user);
        itemRepository.save(item);
    }

    @Test
    void createBooking() {
        BookingDto actualBookingDto = bookingService.createBooking(booker.getId(), bookingCreationDto);

        assertEquals(1, actualBookingDto.getId());
        assertEquals(BookingStatus.WAITING, actualBookingDto.getStatus());
        assertEquals(1, actualBookingDto.getItem().getId());
        assertEquals("name", actualBookingDto.getItem().getName());
        assertEquals(3, actualBookingDto.getBooker().getId());
    }

    @Test
    void considerationOfRequest() {
        Booking savedBooking = bookingRepository.save(booking);
        BookingDto actualBookingDto = bookingService.considerationOfRequest(owner.getId(), savedBooking.getId(), true);

        assertEquals(1, actualBookingDto.getId());
        assertEquals(BookingStatus.APPROVED, actualBookingDto.getStatus());
    }

    @Test
    void getBookingById() {
        Booking savedBooking = bookingRepository.save(booking);
        BookingDto actualBookingDto = bookingService.getBookingById(booker.getId(), savedBooking.getId());

        assertEquals(1, actualBookingDto.getId());
        assertEquals(BookingStatus.WAITING, actualBookingDto.getStatus());
        assertEquals("name", actualBookingDto.getItem().getName());
        assertEquals(3, actualBookingDto.getBooker().getId());
    }

    @Test
    void getAllBookingsByUser_whenIsOwner_thenReturnBookingDto() {
        bookingRepository.save(booking);
        List<BookingDto> actualBookingDto = bookingService.getAllBookingsByUser(owner.getId(), BookingState.ALL,
                0, 10, true);

        assertEquals(1, actualBookingDto.get(0).getId());
        assertEquals(BookingStatus.WAITING, actualBookingDto.get(0).getStatus());
        assertEquals("name", actualBookingDto.get(0).getItem().getName());
        assertEquals(3, actualBookingDto.get(0).getBooker().getId());
    }

    @Test
    void getAllBookingsByUser_whenIsBooker_thenReturnBookingDto() {
        bookingRepository.save(booking);
        List<BookingDto> actualBookingDto = bookingService.getAllBookingsByUser(booker.getId(), BookingState.ALL,
                0, 10, false);

        assertEquals(1, actualBookingDto.get(0).getId());
        assertEquals(BookingStatus.WAITING, actualBookingDto.get(0).getStatus());
        assertEquals("name", actualBookingDto.get(0).getItem().getName());
        assertEquals(3, actualBookingDto.get(0).getBooker().getId());
    }
}