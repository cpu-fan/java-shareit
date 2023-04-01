package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentTextDto;
import ru.practicum.shareit.item.dto.ItemCreationDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/items")
@Validated
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    private static final String HEADER_NAME = "X-Sharer-User-Id";

    @GetMapping
    public List<ItemDto> getAllItems(@RequestHeader(HEADER_NAME) long userId,
                                     @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                     @RequestParam(defaultValue = "10") @Positive int size) {
        return itemService.getOwnerItems(userId, from, size);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader(value = HEADER_NAME, required = false) long userId,
                               @PathVariable long itemId) {
        return itemService.getItemById(userId, itemId);
    }

    @PostMapping
    public ItemCreationDto createItem(@RequestHeader(HEADER_NAME) long userId,
                                      @Valid @RequestBody ItemCreationDto itemDto) {
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemCreationDto updateItem(@RequestHeader(HEADER_NAME) long userId,
                                      @PathVariable long itemId,
                                      @RequestBody ItemCreationDto itemDto) {
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/search")
    public List<ItemCreationDto> searchItem(@RequestParam String text,
                                            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                            @RequestParam(defaultValue = "10") @Positive int size) {
        return itemService.searchItem(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(HEADER_NAME) long userId,
                                 @PathVariable long itemId,
                                 @Valid @RequestBody CommentTextDto commentDto) {
        return itemService.addComment(userId, itemId, commentDto);
    }
}
