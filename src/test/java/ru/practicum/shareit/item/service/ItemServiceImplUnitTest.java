package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingIdBookerIdDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentTextDto;
import ru.practicum.shareit.item.dto.ItemCreationDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplUnitTest {

    @InjectMocks ItemServiceImpl itemService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private UserService userService;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    private User booker;
    private User user;
    private UserDto userDto;
    private Item item;
    private Comment comment;
    private CommentDto commentDto;
    private ItemCreationDto itemCreationDto;
    private ItemDto itemDto;
    private ItemRequest itemRequest;
    private BookingIdBookerIdDto lb;
    private BookingIdBookerIdDto nb;

    @BeforeEach
    void setUp() {
        User owner = new User(1, "owner", "email@owner.com");
        User requestor = new User(2, "requestor", "email@requestor.com");
        booker = new User(3, "booker", "email@booker.com");
        user = new User(4, "user", "email@user.com");
        userDto = new UserDto(user.getId(), user.getName(), user.getEmail());
        itemRequest = new ItemRequest(1, "desc", requestor, LocalDateTime.now());
        item = new Item(1, "name", "desc", true, owner, itemRequest);
        comment = new Comment(1, "text", item, user, LocalDateTime.now());
        commentDto = new CommentDto(comment.getId(), comment.getText(), comment.getAuthor().getName(),
                comment.getCreated());
        lb = new BookingIdBookerIdDto(1, 1);
        nb = new BookingIdBookerIdDto(2, 1);
        itemCreationDto = new ItemCreationDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable());
        itemDto = new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable(), lb, nb,
                List.of(commentDto));
    }

    @Test
    void getOwnerItems() {
        Booking booking = new Booking(1, LocalDateTime.now(), LocalDateTime.now().plusHours(1), item, new User(), BookingStatus.APPROVED);

        when(itemRepository.findByOwnerId(anyLong(), any(Pageable.class))).thenReturn(List.of(item));
        when(commentRepository.findByItemsId(anyList())).thenReturn(List.of(comment));
        when(commentMapper.toDto(any(Comment.class))).thenReturn(commentDto);
        when(bookingRepository.findByItemId(anyLong())).thenReturn(List.of(booking));
        when(bookingMapper.toBookingIdBookerIdDto(any())).thenReturn(lb);
        when(bookingMapper.toBookingIdBookerIdDto(any())).thenReturn(nb);
        when(itemMapper.toDto(any(), any(), any(), anyList())).thenReturn(itemDto);

        List<ItemDto> actualItemsDto = itemService.getOwnerItems(1, 0, 10);

        assertEquals(List.of(itemDto), actualItemsDto);

    }

    @Test
    void getItemById_whenRequestedNotOwner_thenReturnWithoutBookingInfo() {
        itemDto.setLastBooking(null);
        itemDto.setNextBooking(null);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(anyLong())).thenReturn(List.of(comment));
        when(commentMapper.toDto(any(Comment.class))).thenReturn(commentDto);
        when(itemMapper.toDto(any(), any(), any(), anyList())).thenReturn(itemDto);

        ItemDto actualItemDto = itemService.getItemById(2, 1);

        assertEquals(itemDto, actualItemDto);
        assertNull(actualItemDto.getLastBooking());
        assertNull(actualItemDto.getNextBooking());
    }

    @Test
    void getItemById_whenRequestedOwner_thenReturnWithBookingInfo() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(anyLong())).thenReturn(List.of(comment));
        when(commentMapper.toDto(any(Comment.class))).thenReturn(commentDto);
        when(itemMapper.toDto(any(), any(), any(), anyList())).thenReturn(itemDto);

        ItemDto actualItemDto = itemService.getItemById(1, 1);

        assertEquals(itemDto, actualItemDto);
        assertNotNull(actualItemDto.getLastBooking());
        assertNotNull(actualItemDto.getNextBooking());
    }

    @Test
    void getItemById_whenItemNotFound_thenReturnException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItemById(3, 1));

        verify(commentRepository, never()).findByItemsId(anyList());
        verify(bookingRepository, never()).findByItemId(anyLong());
        verify(bookingRepository, never()).findByItemId(anyLong());
        verify(bookingMapper, never()).toBookingIdBookerIdDto(any());
        verify(itemMapper, never()).toDto(any(), any(), any(), anyList());
    }

    @Test
    void createItem_whenUserFoundAndCreateWithoutRequestId_thenReturnItemWithoutRequestInfo() {
        when(userService.getUserById(anyLong())).thenReturn(userDto);
        when(userMapper.toUser(any())).thenReturn(user);
        when(itemMapper.toItem(any(), any())).thenReturn(item);
        when(itemRepository.save(any())).thenReturn(item);
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemCreationDto);

        ItemCreationDto actualItemCreationDto = itemService.createItem(user.getId(), itemCreationDto);

        assertEquals(itemCreationDto, actualItemCreationDto);
        assertEquals(0, actualItemCreationDto.getRequestId());
    }

    @Test
    void createItem_whenUserFoundAndCreateWithRequestId_thenReturnItemWithRequestInfo() {
        itemCreationDto.setRequestId(1);

        when(userService.getUserById(anyLong())).thenReturn(userDto);
        when(userMapper.toUser(any())).thenReturn(user);
        when(itemMapper.toItem(any(), any())).thenReturn(item);
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(any())).thenReturn(item);
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemCreationDto);

        ItemCreationDto actualItemCreationDto = itemService.createItem(user.getId(), itemCreationDto);

        assertEquals(itemCreationDto, actualItemCreationDto);
        assertEquals(1, actualItemCreationDto.getRequestId());
    }

    @Test
    void createItem_whenUserNotFound_thenReturnException() {
        String errorMessage = "Пользователь с id = " + 4 + " не найден";
        when(userService.getUserById(anyLong())).thenThrow(new NotFoundException(errorMessage));

        assertThrows(NotFoundException.class, () -> itemService.createItem(user.getId(), itemCreationDto));

        verify(userMapper, never()).toUser(any());
        verify(itemMapper, never()).toItem(any(), any());
        verify(itemRequestRepository, never()).findById(anyLong());
        verify(itemRepository, never()).save(any());
        verify(itemMapper, never()).toDto(any(Item.class));
    }

    @Test
    void createItem_whenItemRequestNotFound_thenReturnException() {
        itemCreationDto.setRequestId(1);

        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createItem(user.getId(), itemCreationDto));

        verify(userService, times(1)).getUserById(anyLong());
        verify(userMapper, times(1)).toUser(any());
        verify(itemMapper, times(1)).toItem(any(), any());
        verify(itemRequestRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).save(any());
        verify(itemMapper, never()).toDto(any(Item.class));
    }

    @Test
    void updateItem_whenUpdateName_thenReturnUpdatedItem() {
        item.setRequest(null);

        ItemCreationDto nameItemCreationDto = new ItemCreationDto(0, "updatedName", null, null);
        Item newItem = new Item();
        newItem.setName(nameItemCreationDto.getName());

        when(userService.getUserById(anyLong())).thenReturn(userDto);
        when(userMapper.toUser(any())).thenReturn(user);
        when(itemMapper.toItem(any(), any())).thenReturn(newItem);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        itemCreationDto.setName(newItem.getName());
        when(itemRepository.save(any())).thenReturn(item);
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemCreationDto);

        ItemCreationDto actualItemCreationDto = itemService.updateItem(1, 1, nameItemCreationDto);

        assertEquals(nameItemCreationDto.getName(), actualItemCreationDto.getName());
    }

    @Test
    void updateItem_whenUpdateDescription_thenReturnUpdatedItem() {
        item.setRequest(null);

        ItemCreationDto itemCreationDto = new ItemCreationDto(0, null, "updatedDescription", null);
        Item newItem = new Item();
        newItem.setDescription(itemCreationDto.getDescription());

        when(userService.getUserById(anyLong())).thenReturn(userDto);
        when(userMapper.toUser(any())).thenReturn(user);
        when(itemMapper.toItem(any(), any())).thenReturn(newItem);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        itemCreationDto.setName(newItem.getName());
        when(itemRepository.save(any())).thenReturn(item);
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemCreationDto);

        ItemCreationDto actualItemCreationDto = itemService.updateItem(1, 1, itemCreationDto);

        assertEquals(itemCreationDto.getDescription(), actualItemCreationDto.getDescription());
    }

    @Test
    void updateItem_whenUpdateAvailable_thenReturnUpdatedItem() {
        item.setRequest(null);

        ItemCreationDto itemCreationDto = new ItemCreationDto(0, null, null, false);
        Item newItem = new Item();
        newItem.setAvailable(itemCreationDto.getAvailable());

        when(userService.getUserById(anyLong())).thenReturn(userDto);
        when(userMapper.toUser(any())).thenReturn(user);
        when(itemMapper.toItem(any(), any())).thenReturn(newItem);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        itemCreationDto.setName(newItem.getName());
        when(itemRepository.save(any())).thenReturn(item);
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemCreationDto);

        ItemCreationDto actualItemCreationDto = itemService.updateItem(1, 1, itemCreationDto);

        assertEquals(itemCreationDto.getAvailable(), actualItemCreationDto.getAvailable());
    }

    @Test
    void searchItem() {
        when(itemRepository.search(anyString(), any(Pageable.class))).thenReturn(List.of(item));
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemCreationDto);

        List<ItemCreationDto> actualItemCreationDto = itemService.searchItem("name", 0, 10);

        assertEquals(List.of(itemCreationDto), actualItemCreationDto);
    }

    @Test
    void addComment() {
        Booking booking = new Booking(1, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                item, booker, BookingStatus.APPROVED);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userService.getUserById(anyLong())).thenReturn(userDto);
        when(userMapper.toUser(any())).thenReturn(user);
        when(bookingRepository.findByItemIdAndBookerIdAndStatusAndEndBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(List.of(booking));
        when(commentMapper.toComment(any(), any(), any())).thenReturn(comment);
        when(commentRepository.save(any())).thenReturn(comment);
        when(commentMapper.toDto(comment)).thenReturn(commentDto);

        CommentDto actualCommentDto = itemService.addComment(user.getId(), item.getId(), new CommentTextDto("text"));

        assertEquals(commentDto, actualCommentDto);
    }
}