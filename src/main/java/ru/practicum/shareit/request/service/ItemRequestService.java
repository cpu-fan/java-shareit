package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestCreatedDto;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestCreatedDto addRequest(long userId, ItemRequestCreationDto itemRequestDto);

    List<ItemRequestDto> getListOwnRequests(long userId);

    ItemRequestDto getRequestById(long userId, long requestId);

    List<ItemRequestDto> getRequestsList(long userId, int from, int size);
}
