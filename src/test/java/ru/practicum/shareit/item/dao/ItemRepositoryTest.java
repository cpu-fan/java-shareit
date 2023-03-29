package ru.practicum.shareit.item.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        owner = new User(0, "owner", "email@owner.com");
        User requestor = new User(0, "requestor", "email@requestor.com");
        User booker = new User(0, "booker", "email@booker.com");
        User user = new User(0, "user", "email@user.com");
        itemRequest = new ItemRequest(0, "itemRequest desc", requestor, LocalDateTime.now());
        Item item = new Item(0, "item name", "item desc", true, owner, itemRequest);
        Comment comment = new Comment(0, "comment text", item, user, LocalDateTime.now());


        userRepository.save(owner);
        userRepository.save(requestor);
        userRepository.save(booker);
        userRepository.save(user);
        itemRequestRepository.save(itemRequest);
        itemRepository.save(item);
        commentRepository.save(comment);
    }

    @Test
    void search() {
        Pageable pageable = PageRequest.of(0 / 10, 10, Sort.by("id").descending());
        String searchText = "name";
        List<Item> foundItems = itemRepository.search(searchText, pageable);

        assertFalse(foundItems.isEmpty());
        assertTrue(foundItems.get(0).getName().contains(searchText));
    }

    @Test
    void findByOwnerId() {
        Pageable pageable = PageRequest.of(0 / 10, 10, Sort.by("id").descending());
        List<Item> foundItems = itemRepository.findByOwnerId(owner.getId(), pageable);

        assertFalse(foundItems.isEmpty());
        assertEquals(owner.getId(), foundItems.get(0).getOwner().getId());
    }

    @Test
    void findByRequestIdIn() {
        List<Item> foundItems = itemRepository.findByRequestIdIn(List.of(itemRequest.getId()));

        assertFalse(foundItems.isEmpty());
        assertEquals(itemRequest.getId(), foundItems.get(0).getRequest().getId());
    }

    @Test
    void findByRequestId() {
        List<Item> foundItems = itemRepository.findByRequestId(itemRequest.getId());

        assertFalse(foundItems.isEmpty());
        assertEquals(itemRequest.getId(), foundItems.get(0).getRequest().getId());
    }
}