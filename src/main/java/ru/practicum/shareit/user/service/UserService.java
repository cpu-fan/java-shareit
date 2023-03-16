package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getAllUsers();

    UserDto getUserById(long userId);

    UserDto createUser(UserDto user);

    UserDto updateUser(long userId, UserDto user);

    void deleteUser(long userId);
}
