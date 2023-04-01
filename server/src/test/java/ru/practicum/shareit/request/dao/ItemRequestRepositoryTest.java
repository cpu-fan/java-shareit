package ru.practicum.shareit.request.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private User requestor;

    @BeforeEach
    void setUp() {
        requestor = new User(0, "requestor", "email@requestor.com");
        ItemRequest itemRequest = new ItemRequest(0, "ItemRequest desc", requestor, LocalDateTime.now().minusDays(2));

        userRepository.save(requestor);
        itemRequestRepository.save(itemRequest);
    }

    @Test
    void findByRequestorIdOrderByCreatedDesc() {
        ItemRequest secondItemRequest = new ItemRequest(0, "secondItemRequest desc", requestor,
                LocalDateTime.now().minusDays(1));
        itemRequestRepository.save(secondItemRequest);

        List<ItemRequest> foundItemRequests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(requestor.getId());

        assertFalse(foundItemRequests.isEmpty());
        assertEquals(requestor.getId(), foundItemRequests.get(0).getRequestor().getId());
        assertTrue(foundItemRequests.get(0).getId() > foundItemRequests.get(1).getId());
        assertTrue(foundItemRequests.get(0).getCreated().isAfter(foundItemRequests.get(1).getCreated()));
    }
}