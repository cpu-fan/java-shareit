package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDto ownerDto;
    private UserDto requestorDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        User owner = new User(1, "owner", "email@owner.com");
        User requestor = new User(2, "requestor", "email@requestor.com");
        User user = new User(3, "user", "email@user.com");

        ownerDto = new UserDto(owner.getId(), owner.getName(), owner.getEmail());
        requestorDto = new UserDto(requestor.getId(), requestor.getName(), requestor.getEmail());
        userDto = new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    @Test
    @SneakyThrows
    void getAllUsers() {
        List<UserDto> userDtoList = List.of(ownerDto, requestorDto, userDto);
        when(userService.getAllUsers()).thenReturn(userDtoList);

        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(userDtoList.get(0).getId()))
                .andExpect(jsonPath("$[1].id").value(userDtoList.get(1).getId()))
                .andExpect(jsonPath("$[2].id").value(userDtoList.get(2).getId()));
    }

    @Test
    @SneakyThrows
    void getUserById() {
        when(userService.getUserById(anyLong())).thenReturn(userDto);

        mockMvc.perform(get("/users/" + userDto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));
    }

    @Test
    @SneakyThrows
    void create() {
        UserDto newUserDto = new UserDto(4, "new user", "emailnewuser@email.com");
        when(userService.createUser(any())).thenReturn(newUserDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(newUserDto.getId()))
                .andExpect(jsonPath("$.name").value(newUserDto.getName()))
                .andExpect(jsonPath("$.email").value(newUserDto.getEmail()));
    }

    @Test
    @SneakyThrows
    void create_whenNameIsBlank_thenReturnBadRequest() {
        UserDto newUserDto = new UserDto(4, "", "emailnewuser@email.com");

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any());
    }

    @Test
    @SneakyThrows
    void create_whenEmailIsBlank_thenReturnBadRequest() {
        UserDto newUserDto = new UserDto(4, "new user", "");

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any());
    }

    @Test
    @SneakyThrows
    void update() {
        String newName = "newest name";
        userDto.setName(newName);
        when(userService.updateUser(anyLong(), any())).thenReturn(userDto);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/" + userDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(newName))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

    }

    @Test
    @SneakyThrows
    void delete() {
        when(userService.updateUser(anyLong(), any())).thenReturn(userDto);

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/" + userDto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }
}