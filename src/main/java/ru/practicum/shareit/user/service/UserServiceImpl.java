package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.EmailAlreadyExistsException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.user.dao.UserRepositoryInMemImpl;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepositoryInMemImpl userRepository;

    @Override
    public List<User> getAllUsers() {
        log.info("Запрошен список пользователей");
        return new ArrayList<>(userRepository.findAll().values());
    }

    @Override
    public User getUserById(long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            String message = "Пользователь с id = " + userId + " не найден";
            log.error(message);
            throw new NotFoundException(message);
        }
        log.info("Запрошен пользователь с id = " + userId);
        return user;
    }

    @Override
    public User createUser(User user) {
        checkSameEmail(user.getEmail());
        user = userRepository.create(user);
        log.info("Зарегистрирован новый пользователь " + user);
        return user;
    }

    @Override
    public User updateUser(long userId, User user) {
        User updatingUser = getUserById(userId);

        // Конвертирую Item в Map и отбираю только те поля-ключи, значение у которых != null
        Mapper mapper = new Mapper();
        Map<String, Object> userFields = mapper.toMap(user);

        // Далее обновляю значения полей у существующего объекта
        if (userFields.containsKey("name")) {
            updatingUser.setName(user.getName());
        }
        if (userFields.containsKey("email")) {
            String email = user.getEmail();
            checkSameEmail(email);
            updatingUser.setEmail(user.getEmail());
        }

        log.info("Обновлена информация о пользователе " + updatingUser);
        return userRepository.update(userId, updatingUser);
    }

    @Override
    public void deleteUser(long userId) {
        getUserById(userId);
        userRepository.delete(userId);
        log.info("Удален пользователь id = " + userId);
    }

    private List<String> getUserEmails() {
        return userRepository.findAll().values().stream()
                .map(User::getEmail)
                .collect(Collectors.toList());
    }

    private void checkSameEmail(String email) {
        if (getUserEmails().contains(email)) {
            String message = "Аккаунт с почтой " + email + " уже зарегистрирован";
            log.error(message);
            throw new EmailAlreadyExistsException(message);
        }
    }
}
