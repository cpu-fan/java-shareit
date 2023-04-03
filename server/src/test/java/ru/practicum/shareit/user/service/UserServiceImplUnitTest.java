package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplUnitTest {

    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Captor
    ArgumentCaptor<User> userArgumentCaptor;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User(1, "name", "email@userdto.org");
        userDto = new UserDto(1, "name", "email@userdto.org");
    }

    @Test
    void getAllUsers_whenUserIsExists_thenReturnListOfUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        List<UserDto> actualUsers = userService.getAllUsers();

        assertEquals(List.of(userDto), actualUsers);
    }

    @Test
    void getAllUsers_whenUsersIsEmpty_thenReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());
        List<UserDto> actualUsers = userService.getAllUsers();
        assertEquals(List.of(), actualUsers);
    }

    @Test
    void getUserById_whenUserFound_thenReturnUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        UserDto actualUserDto = userService.getUserById(anyLong());

        assertEquals(userDto, actualUserDto);
    }

    @Test
    void getUserById_whenUserNotFound_thenReturnException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(anyLong()));
        verify(userMapper, never()).toDto(any(User.class));
    }

    @Test
    void createUser() {
        // Я так понимаю, что негативные сценарии актуальны для этого метода только в интеграционных тестах?
        // Имею в виду проверки на валидацию которые осуществляются на уровне контроллера и на уникальность почты на уровне БД.
        // Или же лучше добавить негативные сценарии с помощью моков?
        when(userMapper.toUser(any())).thenReturn(user);
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        UserDto actualUserDto = userService.createUser(userDto);

        assertEquals(userDto, actualUserDto);
    }

    @Test
    void updateUser_whenUserFoundAndUpdateName_thenReturnUpdatedUser() {
        User newUser = new User();
        newUser.setName("updated name");

        when(userMapper.toUser(any())).thenReturn(newUser);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        user.setName(newUser.getName());
        when(userRepository.save(any())).thenReturn(user);

        userService.updateUser(1, userMapper.toDto(newUser));

        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertEquals(newUser.getName(), savedUser.getName());
        assertEquals(user.getEmail(), savedUser.getEmail());
    }

    @Test
    void updateUser_whenUserFoundAndUpdateEmail_thenReturnUpdatedUser() {
        User newUser = new User();
        newUser.setName("updated@email.com");

        when(userMapper.toUser(any())).thenReturn(newUser);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        user.setEmail(newUser.getEmail());
        when(userRepository.save(any())).thenReturn(user);

        userService.updateUser(1, userMapper.toDto(newUser));

        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertEquals(user.getName(), savedUser.getName());
        assertEquals(newUser.getEmail(), savedUser.getEmail());
    }

    @Test
    void updateUser_whenUserNotFound_thenReturnException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(1, userDto));
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDto(any(User.class));
    }

    @Test
    void deleteUser_whenUserFound_thenDeleteUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        userService.deleteUser(anyLong());

        verify(userRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void deleteUser_whenUserNotFound_thenReturnException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.deleteUser(anyLong()));
        verify(userRepository, never()).deleteById(anyLong());
    }
}