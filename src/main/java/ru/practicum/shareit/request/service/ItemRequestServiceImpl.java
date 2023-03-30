package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDtoForRequest;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreatedDto;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;

    private final ItemRepository itemRepository;

    private final UserService userService;

    private final ItemRequestMapper itemRequestMapper;

    private final UserMapper userMapper;

    @Override
    public ItemRequestCreatedDto addRequest(long requestorId, ItemRequestCreationDto itemRequestDto) {
        User requestor = userMapper.toUser(userService.getUserById(requestorId));
        ItemRequest itemRequest = itemRequestMapper.toItemRequest(requestor, itemRequestDto);
        itemRequest = itemRequestRepository.save(itemRequest);

        log.info("Добавлен запрос вещи requestId = " + itemRequest.getId());
        return itemRequestMapper.toDto(itemRequest);
    }

    @Override
    public List<ItemRequestDto> getListOwnRequests(long requestorId) {
        userService.getUserById(requestorId);
        List<ItemRequest> itemRequests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(requestorId);

        log.info("Запрошен список собственных запросов вещей пользователем requestorId = " + requestorId);
        return getItemRequestsWithItems(itemRequests);
    }

    @Override
    public ItemRequestDto getRequestById(long userId, long requestId) {
        userService.getUserById(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(() -> {
            String message = "Запрос requestId " + requestId + " не найден";
            log.error(message);
            throw new NotFoundException(message);
        });
        List<ItemDtoForRequest> items = itemRepository.findByRequestId(itemRequest.getId()).stream()
                .map(itemRequestMapper::toItemDtoForReq)
                .collect(Collectors.toList());

        log.info("Запрошен запрос вещей requestId = " + requestId);
        return itemRequestMapper.toItemRequestDto(itemRequest, items);
    }

    @Override
    public List<ItemRequestDto> getRequestsList(long requestorId, int from, int size) {
        userService.getUserById(requestorId);
        List<ItemRequest> itemRequests = itemRequestRepository.findAll(PageRequest.of(from / size, size,
                Sort.by("created").descending())).stream()
                .filter(i -> i.getRequestor().getId() != requestorId)
                .collect(Collectors.toList());

        log.info("Запрошен список запросов вещей");
        return getItemRequestsWithItems(itemRequests);
    }

    private List<ItemRequestDto> getItemRequestsWithItems(List<ItemRequest> itemRequests) {
        Map<Long, List<ItemDtoForRequest>> items = itemRepository.findByRequestIdIn(itemRequests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList())).stream()
                .map(itemRequestMapper::toItemDtoForReq)
                .collect(Collectors.groupingBy(ItemDtoForRequest::getRequestId,
                        Collectors.mapping(i -> i, Collectors.toList())));

        return itemRequests.stream()
                .map(ir -> itemRequestMapper.toItemRequestDto(ir, items.getOrDefault(ir.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }
}
