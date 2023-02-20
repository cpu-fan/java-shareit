package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.model.User;

import java.util.Map;

public interface UserRepository {

    Map<Long, User> findAll();

    User findById(long userId);

    User create(User user);

    User update(long userId, User user);

    void delete(long userId);
}
