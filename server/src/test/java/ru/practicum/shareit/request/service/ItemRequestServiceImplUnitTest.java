package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDtoForRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreatedDto;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplUnitTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void addRequest_whenRequestorFound_thenReturnRequest() {
        User requestor = new User(1, "test-user", "test@email.com");
        UserDto requestorDto = new UserDto(1, "test-user", "test@email.com");
        ItemRequestCreationDto itemRequestDto = new ItemRequestCreationDto("test description");
        ItemRequest itemRequest = new ItemRequest(0, itemRequestDto.getDescription(),
                requestor, LocalDateTime.now());
        ItemRequest itemRequestAfterSave = new ItemRequest(1, itemRequestDto.getDescription(),
                requestor, LocalDateTime.now());
        ItemRequestCreatedDto expectedItemRequestDto = new ItemRequestCreatedDto(itemRequestAfterSave.getId(),
                itemRequestAfterSave.getDescription(), itemRequestAfterSave.getCreated());

        when(userService.getUserById(anyLong())).thenReturn(requestorDto);
        when(userMapper.toUser(any())).thenReturn(requestor);
        when(itemRequestMapper.toItemRequest(any(), any())).thenReturn(itemRequest);
        when(itemRequestRepository.save(any())).thenReturn(itemRequestAfterSave);
        when(itemRequestMapper.toDto(any(ItemRequest.class))).thenReturn(expectedItemRequestDto);

        ItemRequestCreatedDto actualItemRequestDto = itemRequestService.addRequest(1, itemRequestDto);

        assertEquals(expectedItemRequestDto, actualItemRequestDto);
        verify(itemRequestRepository).save(itemRequest);
    }

    @Test
    void addRequest_whenRequestorNotFound_thenReturnNotFoundException() {
        ItemRequestCreationDto itemRequestDto = new ItemRequestCreationDto("test description");

        when(userService.getUserById(anyLong())).thenThrow(new NotFoundException("Пользователь с id = "
                + 1 + " не найден"));

        assertThrows(NotFoundException.class, () -> itemRequestService.addRequest(1, itemRequestDto));
    }

    @Test
    void getListOwnRequest_whenRequestorFound_thenReturnListOwnRequest() {
        ItemRequest ir = new ItemRequest(1, "desc", new User(), LocalDateTime.now());
        Item item = new Item(1, "name", "desc", true, new User(), ir);
        ItemDtoForRequest itemDto = new ItemDtoForRequest(item.getId(), item.getName(), item.getDescription(),
                item.getAvailable(), item.getRequest().getId());
        ItemRequestDto expected = new ItemRequestDto(ir.getId(), ir.getDescription(), ir.getCreated(), List.of(itemDto));

        when(userService.getUserById(anyLong())).thenReturn(new UserDto());
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(anyLong())).thenReturn(List.of(ir));
        when(itemRepository.findByRequestIdIn(anyList())).thenReturn(List.of(item));
        when(itemRequestMapper.toItemDtoForReq(any())).thenReturn(itemDto);
        when(itemRequestMapper.toItemRequestDto(any(), anyList())).thenReturn(expected);

        List<ItemRequestDto> actualItemRequests = itemRequestService.getListOwnRequests(anyLong());

        assertEquals(List.of(expected), actualItemRequests);
    }

    @Test
    void getListOwnRequest_whenRequestorNotFound_thenReturnNotFoundException() {
        when(userService.getUserById(anyLong())).thenThrow(new NotFoundException("Пользователь с id = "
                + 1 + " не найден"));

        assertThrows(NotFoundException.class, () -> itemRequestService.getListOwnRequests(anyLong()));
    }

    @Test
    void getRequestById_whenRequestorAndRequestFound_thenReturnRequest() {
        ItemRequest ir = new ItemRequest(1, "desc", new User(), LocalDateTime.now());
        Item item = new Item(1, "name", "desc", true, new User(), ir);
        ItemDtoForRequest itemDto = new ItemDtoForRequest(item.getId(), item.getName(), item.getDescription(),
                item.getAvailable(), item.getRequest().getId());
        ItemRequestDto expected = new ItemRequestDto(ir.getId(), ir.getDescription(), ir.getCreated(), List.of(itemDto));

        when(userService.getUserById(anyLong())).thenReturn(new UserDto());
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(ir));
        when(itemRequestMapper.toItemDtoForReq(any())).thenReturn(itemDto);
        when(itemRepository.findByRequestId(anyLong())).thenReturn(List.of(item));
        when(itemRequestMapper.toItemRequestDto(any(), anyList())).thenReturn(expected);

        ItemRequestDto actualItemRequests = itemRequestService.getRequestById(0, 1);

        assertEquals(expected, actualItemRequests);
    }

    @Test
    void getRequestById_whenRequestorNotFound_thenReturnNotFoundException() {
        when(userService.getUserById(anyLong())).thenThrow(new NotFoundException("Пользователь с id = "
                + 1 + " не найден"));

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(0, 1));
        verify(itemRequestRepository, never()).findById(anyLong());
        verify(itemRepository, never()).findByRequestIdIn(anyList());
        verify(itemRequestMapper, never()).toItemRequestDto(any(), anyList());
    }

    @Test
    void getRequestById_whenRequestNotFound_thenReturnNotFoundException() {
        when(itemRequestRepository.findById(anyLong())).thenThrow(new NotFoundException("Запрос requestId " + 1 + " не найден"));

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(0, 1));
        verify(userService, times(1)).getUserById(anyLong());
        verify(itemRepository, never()).findByRequestIdIn(anyList());
        verify(itemRequestMapper, never()).toItemRequestDto(any(), anyList());
    }

    @Test
    void getRequestsList_whenRequestorFound_thenReturnRequestsList() {
        ItemRequest ir = new ItemRequest(1, "desc", new User(), LocalDateTime.now());
        Item item = new Item(1, "name", "desc", true, new User(), ir);
        ItemDtoForRequest itemDto = new ItemDtoForRequest(item.getId(), item.getName(), item.getDescription(),
                item.getAvailable(), item.getRequest().getId());
        ItemRequestDto expected = new ItemRequestDto(ir.getId(), ir.getDescription(), ir.getCreated(), List.of(itemDto));

        when(userService.getUserById(anyLong())).thenReturn(new UserDto());
        when(itemRequestRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(ir)));
        when(itemRepository.findByRequestIdIn(anyList())).thenReturn(List.of(item));
        when(itemRequestMapper.toItemDtoForReq(any())).thenReturn(itemDto);
        when(itemRequestMapper.toItemRequestDto(any(), anyList())).thenReturn(expected);

        List<ItemRequestDto> actualItemRequests = itemRequestService.getRequestsList(2, 0, 10);

        assertEquals(List.of(expected), actualItemRequests);
    }

    @Test
    void getRequestsList_whenRequestorNotFound_thenReturnNotFoundException() {
        when(userService.getUserById(anyLong())).thenThrow(new NotFoundException("Пользователь с id = "
                + 1 + " не найден"));

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(0, 1));
        verify(itemRequestRepository, never()).findAll(any(Pageable.class));
        verify(itemRepository, never()).findByRequestIdIn(anyList());
        verify(itemRequestMapper, never()).toItemDtoForReq(any());
        verify(itemRequestMapper, never()).toItemRequestDto(any(), anyList());
    }
}