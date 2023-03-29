package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentTextDto;
import ru.practicum.shareit.item.dto.ItemCreationDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Sql(scripts = "/schema.sql")
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner;
    private User booker;
    private User user;
    private Item item;
    private Comment comment;
    private ItemCreationDto itemCreationDto;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        owner = new User(1, "owner", "email@owner.com");
        User requestor = new User(2, "requestor", "email@requestor.com");
        booker = new User(3, "booker", "email@booker.com");
        user = new User(4, "user", "email@user.com");
        itemRequest = new ItemRequest(1, "desc", requestor, LocalDateTime.now());
        item = new Item(1, "name", "desc", true, owner, itemRequest);
        comment = new Comment(1, "text", item, user, LocalDateTime.now());
        itemCreationDto = new ItemCreationDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable());

        userRepository.save(owner);
        userRepository.save(requestor);
        userRepository.save(booker);
        userRepository.save(user);
        itemRequestRepository.save(itemRequest);
        itemRepository.save(item);
        commentRepository.save(comment);
    }

    @Test
    void getOwnerItems() {
        List<ItemDto> actualItems = itemService.getOwnerItems(owner.getId(), 0, 10);

        assertEquals(1, actualItems.get(0).getId());
        assertEquals(item.getName(), actualItems.get(0).getName());
        assertEquals(item.getDescription(), actualItems.get(0).getDescription());
        assertEquals(item.getAvailable(), actualItems.get(0).getAvailable());
        assertEquals(comment.getId(), actualItems.get(0).getComments().get(0).getId());
        assertEquals(comment.getText(), actualItems.get(0).getComments().get(0).getText());
        assertEquals(comment.getAuthor().getName(), actualItems.get(0).getComments().get(0).getAuthorName());
    }

    @Test
    void getItemById_whenUserIsNotOwner_thenReturnItemWithoutBookings() {
        ItemDto actualItem = itemService.getItemById(user.getId(), item.getId());

        assertEquals(1, actualItem.getId());
        assertEquals(item.getName(), actualItem.getName());
        assertEquals(item.getDescription(), actualItem.getDescription());
        assertEquals(item.getAvailable(), actualItem.getAvailable());
        assertNull(actualItem.getLastBooking());
        assertNull(actualItem.getNextBooking());
        assertEquals(comment.getId(), actualItem.getComments().get(0).getId());
        assertEquals(comment.getText(), actualItem.getComments().get(0).getText());
        assertEquals(comment.getAuthor().getName(), actualItem.getComments().get(0).getAuthorName());
    }

    @Test
    void getItemById_whenUserIsOwner_thenReturnItemWithBookings() {
        LocalDateTime startLB = LocalDateTime.now().minusHours(2);
        LocalDateTime endLB = LocalDateTime.now().minusHours(1);
        Booking lastBooking = new Booking(1, startLB, endLB, item, booker, BookingStatus.APPROVED);
        bookingRepository.save(lastBooking);


        LocalDateTime startNB = LocalDateTime.now().plusHours(1);
        LocalDateTime endNB = LocalDateTime.now().plusHours(2);
        Booking nextBooking = new Booking(2, startNB, endNB, item, booker, BookingStatus.APPROVED);
        bookingRepository.save(nextBooking);

        ItemDto actualItem = itemService.getItemById(owner.getId(), item.getId());

        assertEquals(1, actualItem.getId());
        assertEquals(item.getName(), actualItem.getName());
        assertEquals(item.getDescription(), actualItem.getDescription());
        assertEquals(item.getAvailable(), actualItem.getAvailable());
        assertEquals(lastBooking.getId(), actualItem.getLastBooking().getId());
        assertEquals(lastBooking.getBooker().getId(), actualItem.getLastBooking().getBookerId());
        assertEquals(nextBooking.getId(), actualItem.getNextBooking().getId());
        assertEquals(nextBooking.getBooker().getId(), actualItem.getNextBooking().getBookerId());
        assertEquals(comment.getId(), actualItem.getComments().get(0).getId());
        assertEquals(comment.getText(), actualItem.getComments().get(0).getText());
        assertEquals(comment.getAuthor().getName(), actualItem.getComments().get(0).getAuthorName());
    }

    @Test
    void createItem_whenWithoutRequestId_thenReturnItemCreationDto() {
        ItemCreationDto actualItemCreationDto = itemService.createItem(owner.getId(), itemCreationDto);

        assertEquals(1, actualItemCreationDto.getId());
        assertEquals(itemCreationDto.getName(), actualItemCreationDto.getName());
        assertEquals(itemCreationDto.getDescription(), actualItemCreationDto.getDescription());
        assertEquals(itemCreationDto.getAvailable(), actualItemCreationDto.getAvailable());
    }

    @Test
    void createItem_whenWithRequestId_thenReturnItemCreationDto() {
        itemCreationDto.setRequestId(itemRequest.getId());

        ItemCreationDto actualItemCreationDto = itemService.createItem(owner.getId(), itemCreationDto);

        assertEquals(1, actualItemCreationDto.getId());
        assertEquals(itemCreationDto.getName(), actualItemCreationDto.getName());
        assertEquals(itemCreationDto.getDescription(), actualItemCreationDto.getDescription());
        assertEquals(itemCreationDto.getAvailable(), actualItemCreationDto.getAvailable());
        assertEquals(itemRequest.getId(), actualItemCreationDto.getRequestId());
    }

    @Test
    void updateItem_whenUpdateName_thenReturnItemCreationDtoWithUpdatedName() {
        itemCreationDto.setName("new name");

        ItemCreationDto actualItemCreationDto = itemService.updateItem(owner.getId(), item.getId(), itemCreationDto);

        assertEquals("new name", actualItemCreationDto.getName());
        assertEquals(1, actualItemCreationDto.getId());
    }

    @Test
    void updateItem_whenUpdateDescription_thenReturnItemCreationDtoWithUpdatedDescription() {
        itemCreationDto.setDescription("new description");

        ItemCreationDto actualItemCreationDto = itemService.updateItem(owner.getId(), item.getId(), itemCreationDto);

        assertEquals("new description", actualItemCreationDto.getDescription());
        assertEquals(1, actualItemCreationDto.getId());
    }

    @Test
    void updateItem_whenUpdateAvailable_thenReturnItemCreationDtoWithUpdatedAvailable() {
        itemCreationDto.setAvailable(false);

        ItemCreationDto actualItemCreationDto = itemService.updateItem(owner.getId(), item.getId(), itemCreationDto);

        assertEquals(false, actualItemCreationDto.getAvailable());
        assertEquals(1, actualItemCreationDto.getId());
    }

    @Test
    void searchItem() {
        Item newItem = new Item(0, "GoPro Hero 11", "Экшен-камера GoPro", true, owner, null);
        itemRepository.save(newItem);

        List<ItemCreationDto> actualItemCreationDto = itemService.searchItem("экшен", 0, 10);

        assertEquals(newItem.getName(), actualItemCreationDto.get(0).getName());
        assertEquals(newItem.getDescription(), actualItemCreationDto.get(0).getDescription());
        assertEquals(newItem.getAvailable(), actualItemCreationDto.get(0).getAvailable());
    }

    @Test
    void addComment() {
        Booking newBooking = new Booking(0, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1),
                item, booker, BookingStatus.APPROVED);
        bookingRepository.save(newBooking);
        CommentTextDto commentTextDto = new CommentTextDto("Все хорошо, только у болгарки диск отвалился и прилетел " +
                "в глаз. Благо еще арендовал очки");

        CommentDto actualCommentDto = itemService.addComment(booker.getId(), item.getId(), commentTextDto);

        assertEquals(commentTextDto.getText(), actualCommentDto.getText());
        assertEquals(booker.getName(), actualCommentDto.getAuthorName());
    }
}