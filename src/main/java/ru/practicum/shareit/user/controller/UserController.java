package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final Mapper mapper;

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable long id) {
        User user = userService.getUserById(id);
        return mapper.toDto(user);
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        User user = mapper.toUser(userDto);
        user = userService.createUser(user);
        userDto = mapper.toDto(user);
        return userDto;
    }

    @PatchMapping("/{userId}")
    public UserDto update(@Valid @PathVariable long userId,
                          @RequestBody UserDto userDto) {
        User user = mapper.toUser(userDto);
        user = userService.updateUser(userId, user);
        userDto = mapper.toDto(user);
        return userDto;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        userService.deleteUser(id);
    }
}
