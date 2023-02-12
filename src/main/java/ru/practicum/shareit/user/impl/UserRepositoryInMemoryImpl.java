package ru.practicum.shareit.user.impl;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Repository
public class UserRepositoryInMemoryImpl implements UserRepository {

    // TODO: реализовать хранение в памяти

    @Override
    public List<User> findAllUsers() {
        return null;
    }

    @Override
    public User findUserById(long userId) {
        return null;
    }

    @Override
    public User createUser(User user) {
        return null;
    }

    @Override
    public User updateUser(User user) {
        return null;
    }

    @Override
    public void deleteUser(long userId) {

    }
}
