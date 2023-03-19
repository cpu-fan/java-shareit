package ru.practicum.shareit.user.dao.inmemory;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepositoryImMemory {

    List<User> findAll();

    User findById(long userId);

    User create(User user);

    User update(long userId, User user);

    void delete(long userId);
}
