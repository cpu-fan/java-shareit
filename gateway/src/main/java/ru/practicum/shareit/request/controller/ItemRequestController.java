package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/requests")
@Validated
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {

    private static final String HEADER_NAME = "X-Sharer-User-Id";

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> addRequest(@RequestHeader(HEADER_NAME) long requestorId,
                                             @Valid @RequestBody ItemRequestCreationDto itemRequestDto) {
        log.info("Создание запроса вещи itemRequestDescription = {} пользователем userId = {}",
                itemRequestDto.getDescription(), requestorId);
        return itemRequestClient.addRequest(itemRequestDto, requestorId);
    }

    @GetMapping
    public ResponseEntity<Object> getListOwnRequests(@RequestHeader(HEADER_NAME) long requestorId) {
        log.info("Запрошен список собственных запросов вещей пользователем userId = {}", requestorId);
        return itemRequestClient.getListOwnRequests(requestorId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader(HEADER_NAME) long userId,
                                                 @PathVariable long requestId) {
        log.info("Запрошен запрос вещей requestId = {} пользователем userId = {}", requestId, userId);
        return itemRequestClient.getRequestById(requestId, userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getRequestsList(@RequestHeader(HEADER_NAME) long requestorId,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                  @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Запрошен список всех запросов вещей пользователем userId = {}. Параметры from = {}, size = {}",
                requestorId, from, size);
        return itemRequestClient.getRequestsList(requestorId, from, size);
    }
}
