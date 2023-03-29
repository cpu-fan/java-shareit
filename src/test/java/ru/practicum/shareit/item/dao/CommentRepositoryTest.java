package ru.practicum.shareit.item.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Comment;
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
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Item item;

    @BeforeEach
    void setUp() {
        User owner = new User(0, "owner", "email@owner.com");
        User requestor = new User(0, "requestor", "email@requestor.com");
        User user = new User(0, "user", "email@user.com");
        ItemRequest itemRequest = new ItemRequest(0, "itemRequest desc", requestor, LocalDateTime.now());
        item = new Item(0, "item name", "item desc", true, owner, itemRequest);
        Comment comment = new Comment(0, "comment text", item, user, LocalDateTime.now());

        userRepository.save(owner);
        userRepository.save(requestor);
        userRepository.save(user);
        itemRequestRepository.save(itemRequest);
        itemRepository.save(item);
        commentRepository.save(comment);
    }

    @Test
    void findByItemId() {
        List<Comment> foundComments = commentRepository.findByItemId(item.getId());

        assertFalse(foundComments.isEmpty());
        assertEquals(item.getId(), foundComments.get(0).getItem().getId());
    }

    @Test
    void findByItemsId() {
        List<Comment> foundComments = commentRepository.findByItemsId(List.of(item.getId()));

        assertFalse(foundComments.isEmpty());
        assertEquals(item.getId(), foundComments.get(0).getItem().getId());
    }
}