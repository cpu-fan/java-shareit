package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentTextDto;
import ru.practicum.shareit.item.dto.ItemCreationDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.user.dao.UserRepository;
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
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private final ItemService itemService;

    private final UserService userService;

    private final Mapper mapper;

    private static final String HEADER_NAME = "X-Sharer-User-Id";

    @GetMapping
    public List<ItemDto> getAllItems(@RequestHeader(HEADER_NAME) long userId) {
        return itemService.getOwnerItems(userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader(value = HEADER_NAME, required = false) long userId,
                               @PathVariable long itemId) {
        return itemService.getItemById(userId, itemId);
    }

    @PostMapping
    public ItemCreationDto createItem(@RequestHeader(HEADER_NAME) long userId,
                                      @Valid @RequestBody ItemCreationDto itemDto) {
        User user = userService.getUserById(userId);
        Item item = mapper.toItem(user, itemDto);
        item = itemService.createItem(item);
        itemDto = mapper.toDto(item);
        return itemDto;
    }

    @PatchMapping("/{itemId}")
    public ItemCreationDto updateItem(@RequestHeader(HEADER_NAME) long userId,
                                      @PathVariable long itemId,
                                      @RequestBody ItemCreationDto itemDto) {
        User user = userService.getUserById(userId);
        Item item = mapper.toItem(user, itemDto);
        item = itemService.updateItem(userId, itemId, item);
        itemDto = mapper.toDto(item);
        return itemDto;
    }

    @GetMapping("/search")
    public List<ItemCreationDto> searchUser(@RequestParam String text) {
        return itemService.searchItem(text)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(HEADER_NAME) long userId,
                                 @PathVariable long itemId,
                                 @Valid @RequestBody CommentTextDto comment) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            String message = "Вещь itemId = " + itemId + " не найдена";
            log.error(message);
            throw new NotFoundException(message);
        });
        User author = userRepository.findById(userId).orElseThrow(() -> {
            String message = "Пользователь userId = " + itemId + " не найден";
            log.error(message);
            throw new NotFoundException(message);
        });

        Comment newComment = mapper.toComment(comment, item, author);
        newComment = itemService.addComment(newComment, item.getId(), author.getId());
        return mapper.toDto(newComment);
    }
}
