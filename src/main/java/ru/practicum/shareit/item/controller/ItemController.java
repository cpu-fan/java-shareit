package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;
    private final UserService userService;
    private final Mapper mapper;

    @GetMapping
    public List<ItemDto> getAllItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.getOwnerItems(userId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable long itemId) {
        Item item = itemService.getItemById(itemId);
        return mapper.toDto(item);
    }

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") long userId,
                              @Valid @RequestBody ItemDto itemDto) {
        User user = userService.getUserById(userId);
        Item item = mapper.toItem(user, itemDto);
        item = itemService.createItem(item);
        itemDto = mapper.toDto(item);
        return itemDto;
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                              @PathVariable long itemId,
                              @RequestBody ItemDto itemDto) {
        long itemOwnerId = itemService.getItemById(itemId).getOwner().getId();
        if (userId != itemOwnerId) {
            String message = "Редактирование вещи доступно только ее владельцу";
            log.error(message);
            throw new ForbiddenException(message);
        }
        User user = userService.getUserById(userId);
        Item item = mapper.toItem(user, itemDto);
        item = itemService.updateItem(itemId, item);
        itemDto = mapper.toDto(item);
        return itemDto;
    }

    @GetMapping("/search")
    public List<ItemDto> searchUser(@RequestParam String text) {
        return itemService.searchItem(text)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
