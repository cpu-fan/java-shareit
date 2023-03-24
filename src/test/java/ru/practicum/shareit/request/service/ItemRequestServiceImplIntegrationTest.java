package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.request.dto.ItemRequestCreatedDto;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Transactional
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestServiceImpl itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private Mapper mapper;

    private final User requestor = new User(0, "test-user", "test@email.com");

    @BeforeEach
    void setUp() {
        userRepository.save(requestor);
    }

    @Test
    void addRequest_whenRequestorFound_thenReturnRequest() {
        long requestorId = 1;
        ItemRequestCreationDto itemRequestDto = new ItemRequestCreationDto("test description");
        ItemRequest itemRequest = new ItemRequest(0, itemRequestDto.getDescription(),
                requestor, LocalDateTime.now());

        when(mapper.toItemRequest(requestor, itemRequestDto)).thenReturn(itemRequest);

        ItemRequestCreatedDto actualItemRequestDto = itemRequestService.addRequest(requestorId, itemRequestDto);
        ItemRequestCreatedDto expectedItemRequestDto = new ItemRequestCreatedDto(1, itemRequestDto.getDescription(),
                actualItemRequestDto.getCreated());

        assertEquals(expectedItemRequestDto, actualItemRequestDto);
    }
}