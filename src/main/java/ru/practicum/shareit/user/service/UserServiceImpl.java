package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final Mapper mapper;

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Запрошен список пользователей");
        return new ArrayList<>(userRepository.findAll()).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            String message = "Пользователь с id = " + userId + " не найден";
            log.error(message);
            throw new NotFoundException(message);
        }
        log.info("Запрошен пользователь с id = " + userId);
        return mapper.toDto(user.get());
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = mapper.toUser(userDto);
        user = userRepository.save(user);
        log.info("Зарегистрирован новый пользователь " + user);
        return mapper.toDto(user);
    }

    @Override
    public UserDto updateUser(long userId, UserDto userDto) {
        User user = mapper.toUser(userDto);
        User updatingUser = userRepository.findById(userId).orElseThrow(() -> {
            String message = "Пользователь с id = " + userId + " не найден";
            log.error(message);
            throw new NotFoundException(message);
        });

        // Конвертирую Item в Map и отбираю только те поля-ключи, значение у которых != null
        Mapper mapper = new Mapper();
        Map<String, Object> userFields = mapper.toMap(user);

        // Далее обновляю значения полей у существующего объекта
        if (userFields.containsKey("name")) {
            updatingUser.setName(user.getName());
        }
        if (userFields.containsKey("email")) {
            updatingUser.setEmail(user.getEmail());
        }

        log.info("Обновлена информация о пользователе " + updatingUser);
        updatingUser = userRepository.save(updatingUser);
        return mapper.toDto(updatingUser);
    }

    @Override
    public void deleteUser(long userId) {
        getUserById(userId);
        userRepository.deleteById(userId);
        log.info("Удален пользователь id = " + userId);
    }
}
