package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDtoForRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreatedDto;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;

    private final ItemRepository itemRepository;

    private final UserService userService;

    private final Mapper mapper;

    @Override
    public ItemRequestCreatedDto addRequest(long requestorId, ItemRequestCreationDto itemRequestDto) {
        User requestor = mapper.toUser(userService.getUserById(requestorId));
        ItemRequest itemRequest = mapper.toItemRequest(requestor, itemRequestDto);
        itemRequest = itemRequestRepository.save(itemRequest);

        log.info("Добавлен запрос вещи requestId = " + itemRequest.getId());
        return mapper.toDto(itemRequest);
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
                .map(mapper::toItemDtoForReq)
                .collect(Collectors.toList());

        log.info("Запрошен запрос вещей requestId = " + requestId);
        return mapper.toItemRequestDto(itemRequest, items);
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
        List<Item> items = itemRepository.findByRequestIdIn(itemRequests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList()));

        return itemRequests.stream()
                .map(ir -> mapper.toItemRequestDto(ir, items.stream()
                        .filter(item -> item.getRequest().getId() == ir.getId())
                        .map(mapper::toItemDtoForReq)
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }
}
