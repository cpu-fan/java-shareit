package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {

    List<User> findAllUsers();

    User findUserById(long userId);

    User createUser(User user);

    User updateUser(User user);

    void deleteUser(long userId);
}
