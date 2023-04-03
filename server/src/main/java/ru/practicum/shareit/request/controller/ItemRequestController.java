package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreatedDto;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private static final String HEADER_NAME = "X-Sharer-User-Id";

    private final ItemRequestServiceImpl itemRequestService;

    @PostMapping
    public ItemRequestCreatedDto addRequest(@RequestHeader(HEADER_NAME) long requestorId,
                                            @RequestBody ItemRequestCreationDto itemRequestDto) {
        return itemRequestService.addRequest(requestorId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getListOwnRequests(@RequestHeader(HEADER_NAME) long requestorId) {
        return itemRequestService.getListOwnRequests(requestorId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader(HEADER_NAME) long userId,
                                         @PathVariable long requestId) {
        return itemRequestService.getRequestById(userId, requestId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getRequestsList(@RequestHeader(HEADER_NAME) long requestorId,
                                                @RequestParam int from,
                                                @RequestParam int size) {
        return itemRequestService.getRequestsList(requestorId, from, size);
    }
}
