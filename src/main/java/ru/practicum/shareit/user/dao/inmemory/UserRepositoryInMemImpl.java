package ru.practicum.shareit.user.dao.inmemory;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("UserRepositoryInMemImpl")
public class UserRepositoryInMemImpl implements UserRepositoryImMemory {

    private final Map<Long, User> userMap = new HashMap<>();
    private static long id = 0;

    @Override
    public List<User> findAll() {
        return new ArrayList<>(userMap.values());
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
