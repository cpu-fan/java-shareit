package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentTextDto;
import ru.practicum.shareit.item.dto.ItemCreationDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/items")
@Validated
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemClient itemClient;

    private static final String HEADER_NAME = "X-Sharer-User-Id";

    @GetMapping
    public ResponseEntity<Object> getAllItems(@RequestHeader(HEADER_NAME) long userId,
                                              @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                              @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Запрос списка вещей пользователем userId = {}. Параметры from = {}, size = {}", userId, from, size);
        return itemClient.getOwnerItems(userId, from, size);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(value = HEADER_NAME, required = false) long userId,
                                              @PathVariable long itemId) {
        log.info("Запрос вещи itemId = {} пользователем userId = {}", itemId, userId);
        return itemClient.getItemById(itemId, userId);
    }

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(HEADER_NAME) long userId,
                                             @Valid @RequestBody ItemCreationDto itemDto) {
        log.info("Добавление вещи itemName = {} пользователем userId = {}", itemDto.getName(), userId);
        return itemClient.createItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(HEADER_NAME) long userId,
                                             @PathVariable long itemId,
                                             @RequestBody ItemCreationDto itemDto) {
        log.info("Обновление вещи itemId = {} пользователем userId = {}", itemId, userId);
        return itemClient.updateItem(itemId, itemDto, userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItem(@RequestParam String text,
                                             @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                             @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Поиск вещи по запросу text = \"{}\". Параметры from = {}, size = {}", text, from, size);
        return itemClient.searchItem(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(HEADER_NAME) long userId,
                                             @PathVariable long itemId,
                                             @Valid @RequestBody CommentTextDto commentDto) {
        log.info("Добавление комментария вещи itemId = {} пользователем userId = {}", itemId, userId);
        return itemClient.addComment(commentDto, itemId, userId);
    }
}
