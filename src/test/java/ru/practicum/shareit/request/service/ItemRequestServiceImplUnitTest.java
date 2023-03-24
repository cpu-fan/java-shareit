package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreatedDto;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplUnitTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

//    @Mock
//    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void addRequest_whenRequestorFound_thenReturnRequest() {
        long requestorId = 1;
        User requestor = new User(1, "test-user", "test@email.com");
        UserDto requestorDto = new UserDto(1, "test-user", "test@email.com");

        ItemRequestCreationDto itemRequestDto = new ItemRequestCreationDto("test description");
        ItemRequest itemRequest = new ItemRequest(0, itemRequestDto.getDescription(),
                requestor, LocalDateTime.now());
        ItemRequest itemRequestAfterSave = new ItemRequest(1, itemRequestDto.getDescription(),
                requestor, LocalDateTime.now());
        ItemRequestCreatedDto expectedItemRequestDto = new ItemRequestCreatedDto(itemRequestAfterSave.getId(),
                itemRequestAfterSave.getDescription(), itemRequestAfterSave.getCreated());

        when(userService.getUserById(requestorId)).thenReturn(requestorDto);
        when(mapper.toUser(requestorDto)).thenReturn(requestor);

        when(mapper.toItemRequest(requestor, itemRequestDto)).thenReturn(itemRequest);
        when(itemRequestRepository.save(itemRequest)).thenReturn(itemRequestAfterSave);
        when(mapper.toDto(itemRequestAfterSave)).thenReturn(expectedItemRequestDto);

        ItemRequestCreatedDto actualItemRequestDto = itemRequestService.addRequest(requestorId, itemRequestDto);

        assertEquals(expectedItemRequestDto, actualItemRequestDto);
        verify(itemRequestRepository).save(itemRequest);
    }

    @Test
    void addRequest_whenRequestorNotFound_thenReturnNotFoundException() {
        long requestorId = 1;
        ItemRequestCreationDto itemRequestDto = new ItemRequestCreationDto("test description");

        when(userService.getUserById(requestorId)).thenThrow(new NotFoundException("Пользователь с id = "
                + requestorId + " не найден"));

        assertThrows(NotFoundException.class, () -> itemRequestService.addRequest(requestorId, itemRequestDto));
    }
}