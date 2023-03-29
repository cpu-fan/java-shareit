package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import ru.practicum.shareit.item.dto.ItemIdNameDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserIdDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplUnitTest {

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private Mapper mapper;

    @Captor
    ArgumentCaptor<Booking> bookingArgumentCaptor;

    private User owner;
    private User booker;
    private User user;
    private UserDto userDto;
    private Item item;
    private ItemRequest itemRequest;
    private Booking booking;
    private BookingCreationDto bookingCreationDto;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        owner = new User(1, "owner", "email@owner.com");
        User requestor = new User(2, "requestor", "email@requestor.com");
        booker = new User(3, "booker", "email@booker.com");
        user = new User(4, "user", "email@user.com");
        item = new Item(1, "name", "desc", true, owner, itemRequest);
        bookingCreationDto = new BookingCreationDto(item.getId(), LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2));
        booking = new Booking(1, bookingCreationDto.getStart(), bookingCreationDto.getEnd(), item, booker,
                BookingStatus.WAITING);
        itemRequest = new ItemRequest(1, "desc", requestor, LocalDateTime.now());

        userDto = new UserDto(user.getId(), user.getName(), user.getEmail());
        UserIdDto userIdDto = new UserIdDto(booking.getBooker().getId());
        ItemIdNameDto itemIdNameDto = new ItemIdNameDto(item.getId(), item.getName());
        bookingDto = new BookingDto(booking.getId(), booking.getStart(), booking.getEnd(),
                BookingStatus.APPROVED, userIdDto, itemIdNameDto);
    }

    @Test
    void createBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemId(anyLong())).thenReturn(Collections.emptyList());
        when(mapper.toBooking(any(), any(), any())).thenReturn(booking);
        when(bookingRepository.save(any())).thenReturn(booking);
        when(mapper.toDto(booking)).thenReturn(bookingDto);

        BookingDto actualBookingDto = bookingService.createBooking(booker.getId(), bookingCreationDto);

        assertEquals(bookingDto, actualBookingDto);
    }

    @Test
    void createBooking_whenBookerIsOwner_thenReturnValidationException() {
        BookingCreationDto bookingCreationDto = new BookingCreationDto(item.getId(), LocalDateTime.now().plusHours(3),
                LocalDateTime.now().plusHours(3));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(owner.getId(), bookingCreationDto));

        verify(bookingRepository, never()).findByItemId(anyLong());
        verify(mapper, never()).toDto(any(Booking.class));
        verify(bookingRepository, never()).save(any());
        verify(mapper, never()).toDto(any(Booking.class));
    }

    @Test
    void createBooking_whenItemIsNotAvailable_thenReturnValidationException() {
        item.setAvailable(false);
        BookingCreationDto bookingCreationDto = new BookingCreationDto(item.getId(), LocalDateTime.now().plusHours(3),
                LocalDateTime.now().plusHours(3));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.createBooking(booker.getId(), bookingCreationDto));

        verify(bookingRepository, never()).findByItemId(anyLong());
        verify(mapper, never()).toDto(any(Booking.class));
        verify(bookingRepository, never()).save(any());
        verify(mapper, never()).toDto(any(Booking.class));
    }

    @Test
    void createBooking_whenEndIsBeforeStart_thenReturnValidationException() {
        BookingCreationDto bookingCreationDto = new BookingCreationDto(item.getId(), LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(1));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.createBooking(booker.getId(), bookingCreationDto));

        verify(bookingRepository, never()).findByItemId(anyLong());
        verify(mapper, never()).toDto(any(Booking.class));
        verify(bookingRepository, never()).save(any());
        verify(mapper, never()).toDto(any(Booking.class));
    }

    @Test
    void createBooking_whenBookingsIsOverlapping_thenReturnConflictException() {
        BookingCreationDto bookingCreationDto = new BookingCreationDto(item.getId(), LocalDateTime.now(),
                LocalDateTime.now().plusHours(2));
        Booking existingBooking = new Booking(42, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                item, booker, BookingStatus.APPROVED);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemId(anyLong())).thenReturn(List.of(existingBooking));

        assertThrows(ConflictException.class, () -> bookingService.createBooking(booker.getId(), bookingCreationDto));

        verify(bookingRepository, times(1)).findByItemId(anyLong());
        verify(mapper, never()).toDto(any(Booking.class));
        verify(bookingRepository, never()).save(any());
        verify(mapper, never()).toDto(any(Booking.class));
    }

    @Test
    void createBooking_whenUserNotFound_thenReturnNotFoundException() {
        BookingCreationDto bookingCreationDto = new BookingCreationDto(item.getId(), LocalDateTime.now(),
                LocalDateTime.now().plusHours(2));

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(42, bookingCreationDto));

        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).findByItemId(anyLong());
        verify(mapper, never()).toDto(any(Booking.class));
        verify(bookingRepository, never()).save(any());
        verify(mapper, never()).toDto(any(Booking.class));
    }

    @Test
    void createBooking_whenItemNotFound_thenReturnNotFoundException() {
        BookingCreationDto bookingCreationDto = new BookingCreationDto(42, LocalDateTime.now(),
                LocalDateTime.now().plusHours(2));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(booker.getId(), bookingCreationDto));

        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByItemId(anyLong());
        verify(mapper, never()).toDto(any(Booking.class));
        verify(bookingRepository, never()).save(any());
        verify(mapper, never()).toDto(any(Booking.class));
    }

    @Test
    void considerationOfRequest_whenIsOwnerAndSetApprove_thenReturnApprovedBookingDto() {
        bookingDto.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(mapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        bookingService.considerationOfRequest(owner.getId(), booking.getId(), true);
        verify(bookingRepository).save(bookingArgumentCaptor.capture());
        BookingStatus actualStatus = bookingArgumentCaptor.getValue().getStatus();

        assertEquals(BookingStatus.APPROVED, actualStatus);
    }

    @Test
    void considerationOfRequest_whenIsOwnerAndSetReject_thenReturnRejectedBookingDto() {
        bookingDto.setStatus(BookingStatus.REJECTED);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(mapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        bookingService.considerationOfRequest(owner.getId(), booking.getId(), false);
        verify(bookingRepository).save(bookingArgumentCaptor.capture());
        BookingStatus actualStatus = bookingArgumentCaptor.getValue().getStatus();

        assertEquals(BookingStatus.REJECTED, actualStatus);
    }

    @Test
    void considerationOfRequest_whenBookingHasAlreadyBeenConfirmed_thenReturnValidationException() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> bookingService.considerationOfRequest(owner.getId(),
                booking.getId(), true));
    }

    @Test
    void considerationOfRequest_whenUserHasNoBookingsFound_thenReturnNotFoundException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.considerationOfRequest(user.getId(),
                booking.getId(), true));
    }

    @Test
    void considerationOfRequest_whenBookingNotFound_thenReturnNotFoundException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.considerationOfRequest(user.getId(),
                booking.getId(), true));
    }

    @Test
    void getBookingById() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(mapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        BookingDto actualBookingDto = bookingService.getBookingById(booker.getId(), booking.getId());

        assertEquals(bookingDto, actualBookingDto);
    }

    @Test
    void getBookingById_whenBookingNotFound_thenReturnNotFoundException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(1, 1));
    }

    @Test
    void getBookingById_whenUserNotBookerOrOwner_thenReturnNotFoundException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(user.getId(), booking.getId()));
    }

    @Test
    void getAllBookingsByUser_whenIsOwnerAndStateIsWaiting_thenReturnBookingDto() {
        when(userService.getUserById(anyLong())).thenReturn(userDto);
        when(bookingRepository.findByOwnerId(anyLong(), any())).thenReturn(List.of(booking));
        when(mapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        List<BookingDto> actualBookingDto = bookingService.getAllBookingsByUser(owner.getId(), BookingState.WAITING,
                0, 10, true);

        assertEquals(List.of(bookingDto), actualBookingDto);
        verify(mapper).toDto(bookingArgumentCaptor.capture());
        BookingStatus actualStatus = bookingArgumentCaptor.getValue().getStatus();
        assertEquals(BookingStatus.WAITING, actualStatus);
    }

    @Test
    void getAllBookingsByUser_whenIsOwnerAndStateIsRejected_thenReturnBookingDto() {
        booking.setStatus(BookingStatus.REJECTED);

        when(userService.getUserById(anyLong())).thenReturn(userDto);
        when(bookingRepository.findByOwnerId(anyLong(), any())).thenReturn(List.of(booking));
        when(mapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        List<BookingDto> actualBookingDto = bookingService.getAllBookingsByUser(owner.getId(), BookingState.REJECTED,
                0, 10, true);

        assertEquals(List.of(bookingDto), actualBookingDto);
        verify(mapper).toDto(bookingArgumentCaptor.capture());
        BookingStatus actualStatus = bookingArgumentCaptor.getValue().getStatus();
        assertEquals(BookingStatus.REJECTED, actualStatus);
    }

    @Test
    void getAllBookingsByUser_whenIsOwnerAndStateIsPast_thenReturnBookingDto() {
        booking.setStart(LocalDateTime.now().minusHours(2));
        booking.setEnd(LocalDateTime.now().minusHours(1));

        when(userService.getUserById(anyLong())).thenReturn(userDto);
        when(bookingRepository.findByOwnerId(anyLong(), any())).thenReturn(List.of(booking));
        when(mapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        bookingService.getAllBookingsByUser(owner.getId(), BookingState.PAST, 0, 10, true);

        verify(mapper).toDto(bookingArgumentCaptor.capture());
        Booking actualBooking = bookingArgumentCaptor.getValue();

        assertTrue(actualBooking.getEnd().isBefore(LocalDateTime.now()));
    }

    @Test
    void getAllBookingsByUser_whenIsOwnerAndStateIsCurrent_thenReturnBookingDto() {
        booking.setStart(LocalDateTime.now().minusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(1));

        when(userService.getUserById(anyLong())).thenReturn(userDto);
        when(bookingRepository.findByOwnerId(anyLong(), any())).thenReturn(List.of(booking));
        when(mapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        bookingService.getAllBookingsByUser(owner.getId(), BookingState.CURRENT, 0, 10, true);

        verify(mapper).toDto(bookingArgumentCaptor.capture());
        Booking actualBooking = bookingArgumentCaptor.getValue();

        assertTrue(actualBooking.getStart().isBefore(LocalDateTime.now()));
        assertTrue(actualBooking.getEnd().isAfter(LocalDateTime.now()));
    }

    @Test
    void getAllBookingsByUser_whenIsOwnerAndStateIsFuture_thenReturnBookingDto() {
        booking.setStart(LocalDateTime.now().plusHours(2));
        booking.setEnd(LocalDateTime.now().plusHours(3));

        when(userService.getUserById(anyLong())).thenReturn(userDto);
        when(bookingRepository.findByOwnerId(anyLong(), any())).thenReturn(List.of(booking));
        when(mapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        bookingService.getAllBookingsByUser(owner.getId(), BookingState.FUTURE, 0, 10, true);

        verify(mapper).toDto(bookingArgumentCaptor.capture());
        Booking actualBooking = bookingArgumentCaptor.getValue();

        assertTrue(actualBooking.getStart().isAfter(LocalDateTime.now()));
    }

    @Test
    void getAllBookingsByUser_whenIsOwnerAndStateIsAll_thenReturnBookingDto() {
        when(userService.getUserById(anyLong())).thenReturn(userDto);
        when(bookingRepository.findByOwnerId(anyLong(), any())).thenReturn(List.of(booking));
        when(mapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        List<BookingDto> actualBookingDto = bookingService.getAllBookingsByUser(owner.getId(), BookingState.ALL,
                0, 10, true);

        assertEquals(List.of(bookingDto), actualBookingDto);
        verify(mapper).toDto(bookingArgumentCaptor.capture());
        Booking actualBooking = bookingArgumentCaptor.getValue();
        assertEquals(actualBooking, booking);
    }

    @Test
    void getAllBookingsByUser_whenIsOwnerAndStateIsNull_thenReturnBookingDtoWithAllState() {
        when(userService.getUserById(anyLong())).thenReturn(userDto);
        when(bookingRepository.findByOwnerId(anyLong(), any())).thenReturn(List.of(booking));
        when(mapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        List<BookingDto> actualBookingDto = bookingService.getAllBookingsByUser(owner.getId(), null,
                0, 10, true);

        assertEquals(List.of(bookingDto), actualBookingDto);
        verify(mapper).toDto(bookingArgumentCaptor.capture());
        Booking actualBooking = bookingArgumentCaptor.getValue();
        assertEquals(actualBooking, booking);
    }

    @Test
    void getAllBookingsByUser_whenIsUserAndStateIsAll_thenReturnBookingDtoWithAllState() {
        when(userService.getUserById(anyLong())).thenReturn(userDto);
        when(bookingRepository.findByBookerId(anyLong(), any())).thenReturn(List.of(booking));
        when(mapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        List<BookingDto> actualBookingDto = bookingService.getAllBookingsByUser(user.getId(), BookingState.ALL,
                0, 10, false);

        assertEquals(List.of(bookingDto), actualBookingDto);
        verify(mapper).toDto(bookingArgumentCaptor.capture());
        Booking actualBooking = bookingArgumentCaptor.getValue();
        assertEquals(actualBooking, booking);
    }

    @Test
    void getAllBookingsByUser_whenUserNotFound_thenReturnNotFoundException() {
        when(userService.getUserById(anyLong())).thenThrow(new NotFoundException("Пользователь с id = "
                + user.getId() + " не найден"));

        assertThrows(NotFoundException.class, () -> bookingService.getAllBookingsByUser(user.getId(), null,
                0, 10, false));
    }
}