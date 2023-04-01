package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
@Sql(scripts = "/schema.sql")
class UserServiceImplIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User owner;
    private User requestor;
    private User user;

    @BeforeEach
    void setUp() {
        owner = new User(1, "owner", "email@owner.com");
        requestor = new User(2, "requestor", "email@requestor.com");
        user = new User(3, "user", "email@user.com");

        userRepository.save(owner);
        userRepository.save(requestor);
        userRepository.save(user);
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void getAllUsers() {
        List<UserDto> actualUserDtoList = userService.getAllUsers();

        assertEquals(3, actualUserDtoList.size());
        assertEquals(1, actualUserDtoList.get(0).getId());
        assertEquals(owner.getName(), actualUserDtoList.get(0).getName());
        assertEquals(2, actualUserDtoList.get(1).getId());
        assertEquals(requestor.getName(), actualUserDtoList.get(1).getName());
        assertEquals(3, actualUserDtoList.get(2).getId());
        assertEquals(user.getName(), actualUserDtoList.get(2).getName());
    }

    @Test
    void getUserById() {
        UserDto actualUserDto = userService.getUserById(user.getId());

        assertEquals(user.getId(), actualUserDto.getId());
        assertEquals(user.getName(), actualUserDto.getName());
        assertEquals(user.getEmail(), actualUserDto.getEmail());
    }

    @Test
    void createUser() {
        UserDto newUser = new UserDto(0, "new user", "new@email.com");
        UserDto actualUserDto = userService.createUser(newUser);

        assertEquals(userService.getAllUsers().size(), actualUserDto.getId());
        assertEquals(newUser.getName(), actualUserDto.getName());
        assertEquals(newUser.getEmail(), actualUserDto.getEmail());
    }

    @Test
    void updateUser_whenNameUpdate_thenReturnUpdatedUserDto() {
        UserDto updateUserName = new UserDto(0, "Eren Yeager", null);
        UserDto actualUserDto = userService.updateUser(user.getId(), updateUserName);

        assertEquals(user.getId(), actualUserDto.getId());
        assertEquals(updateUserName.getName(), actualUserDto.getName());
        assertEquals(user.getEmail(), actualUserDto.getEmail());
    }

    @Test
    void updateUser_whenEmailUpdate_thenReturnUpdatedUserDto() {
        UserDto updateUserEmail = new UserDto(0, null, "erenyeager@paradise.com");
        UserDto actualUserDto = userService.updateUser(user.getId(), updateUserEmail);

        assertEquals(user.getId(), actualUserDto.getId());
        assertEquals(user.getName(), actualUserDto.getName());
        assertEquals(updateUserEmail.getEmail(), actualUserDto.getEmail());
    }

    @Test
    void deleteUser() {
        userService.deleteUser(requestor.getId());
        List<UserDto> actualUserDtoList = userService.getAllUsers();

        assertEquals(2, userService.getAllUsers().size());

        assertEquals(owner.getId(), actualUserDtoList.get(0).getId());
        assertEquals(owner.getName(), actualUserDtoList.get(0).getName());
        assertNotEquals(requestor.getId(), actualUserDtoList.get(1).getId());
        assertNotEquals(requestor.getName(), actualUserDtoList.get(1).getName());
        assertEquals(user.getId(), actualUserDtoList.get(1).getId());
        assertEquals(user.getName(), actualUserDtoList.get(1).getName());
    }
}