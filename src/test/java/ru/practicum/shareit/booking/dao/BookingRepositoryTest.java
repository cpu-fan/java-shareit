package ru.practicum.shareit.booking.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    ItemRequestRepository itemRequestRepository;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = new User(0, "owner", "email@owner.com");
        User requestor = new User(0, "requestor", "email@requestor.com");
        booker = new User(0, "booker", "email@booker.com");
        User user = new User(0, "user", "email@user.com");
        ItemRequest itemRequest = new ItemRequest(0, "itemRequest desc", requestor, LocalDateTime.now());
        item = new Item(0, "item name", "item desc", true, owner, itemRequest);
        booking = new Booking(0, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), item, booker,
                BookingStatus.WAITING);

        bookingRepository.save(booking);
        userRepository.save(owner);
        userRepository.save(requestor);
        userRepository.save(booker);
        userRepository.save(user);
        itemRequestRepository.save(itemRequest);
        itemRepository.save(item);
    }

    @Test
    void findByItemId() {
        List<Booking> foundBookings = bookingRepository.findByItemId(item.getId());

        assertFalse(foundBookings.isEmpty());
        assertEquals(item.getId(), foundBookings.get(0).getItem().getId());
    }

    @Test
    void findByBookerId() {
        Pageable pageable = PageRequest.of(0 / 10, 10, Sort.by("start").descending());
        List<Booking> foundBookings = bookingRepository.findByBookerId(booker.getId(), pageable);

        assertFalse(foundBookings.isEmpty());
        assertEquals(booker.getId(), foundBookings.get(0).getBooker().getId());
    }

    @Test
    void findByOwnerId() {
        Pageable pageable = PageRequest.of(0 / 10, 10, Sort.by("start").descending());
        List<Booking> foundBookings = bookingRepository.findByOwnerId(owner.getId(), pageable);

        assertFalse(foundBookings.isEmpty());
        assertEquals(owner.getId(), foundBookings.get(0).getItem().getOwner().getId());
    }

    @Test
    void findByItemIdAndBookerIdAndStatusAndEndBefore() {
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        bookingRepository.save(booking);
        List<Booking> foundBookings = bookingRepository.findByItemIdAndBookerIdAndStatusAndEndBefore(item.getId(),
                booker.getId(), BookingStatus.APPROVED, LocalDateTime.now());

        assertFalse(foundBookings.isEmpty());
        assertEquals(item.getId(), foundBookings.get(0).getItem().getId());
        assertEquals(booker.getId(), foundBookings.get(0).getBooker().getId());
    }
}