package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreatedDto;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@Sql(scripts = "/schema.sql")
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestServiceImpl itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User requestor;
    private User user;
    private ItemRequest ir;
    private ItemRequestCreationDto itemRequestCreationDto;

    @BeforeEach
    void setUp() {
        User owner = new User(1, "owner", "ItemRequestServiceImplIntegrationTest@owner.com");
        requestor = new User(2, "requestor", "ItemRequestServiceImplIntegrationTest@requestor.com");
        user = new User(3, "user", "ItemRequestServiceImplIntegrationTest@user.com");
        ir = new ItemRequest(1, "desc", requestor, LocalDateTime.now());
        itemRequestCreationDto = new ItemRequestCreationDto(ir.getDescription());
        Item item = new Item(1, "name", "desc", true, owner, ir);

        userRepository.save(owner);
        userRepository.save(requestor);
        userRepository.save(user);
        itemRequestRepository.save(ir);
        itemRepository.save(item);
    }

    @Test
    void addRequest() {
        ItemRequestCreatedDto actualItemRequestCreatedDto = itemRequestService.addRequest(requestor.getId(),
                itemRequestCreationDto);

        assertEquals(2, actualItemRequestCreatedDto.getId());
        assertEquals(ir.getDescription(), actualItemRequestCreatedDto.getDescription());
    }

    @Test
    void getListOwnRequests() {
        List<ItemRequestDto> actualItemRequestDto = itemRequestService.getListOwnRequests(requestor.getId());

        assertEquals(1, actualItemRequestDto.get(0).getId());
        assertEquals(ir.getDescription(), actualItemRequestDto.get(0).getDescription());
        assertFalse(actualItemRequestDto.get(0).getItems().isEmpty());
    }

    @Test
    void getRequestById() {
        ItemRequestDto actualItemRequestDto = itemRequestService.getRequestById(user.getId(), ir.getId());

        assertEquals(1, actualItemRequestDto.getId());
        assertEquals(ir.getDescription(), actualItemRequestDto.getDescription());
        assertFalse(actualItemRequestDto.getItems().isEmpty());
    }

    @Test
    void getRequestsList() {
        List<ItemRequestDto> actualItemRequestDto = itemRequestService.getRequestsList(user.getId(), 0,10);

        assertEquals(1, actualItemRequestDto.get(0).getId());
        assertEquals(ir.getDescription(), actualItemRequestDto.get(0).getDescription());
        assertFalse(actualItemRequestDto.get(0).getItems().isEmpty());
    }
}