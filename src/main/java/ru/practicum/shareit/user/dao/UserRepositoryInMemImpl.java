package ru.practicum.shareit.user.dao;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserRepositoryInMemImpl implements UserRepository {

    private final Map<Long, User> userMap = new HashMap<>();
    private static long id = 0;

    @Override
    public Map<Long, User> findAll() {
        return userMap;
    }

    @Override
    public User findById(long userId) {
        return userMap.get(userId);
    }

    @Override
    public User create(User user) {
        user.setId(generateId());
        userMap.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(long userId, User user) {
        return userMap.put(userId, user);
    }

    @Override
    public void delete(long userId) {
        userMap.remove(userId);
    }

    private long generateId() {
        return ++id;
    }
}
