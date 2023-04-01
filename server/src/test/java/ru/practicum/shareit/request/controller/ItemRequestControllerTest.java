package ru.practicum.shareit.request.controller;

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
import ru.practicum.shareit.item.dto.ItemDtoForRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreatedDto;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestServiceImpl itemRequestService;

    private static final String HEADER_NAME = "X-Sharer-User-Id";

    private User requestor;
    private User user;
    private Item item;
    private ItemRequest ir;
    private ItemRequestCreationDto itemRequestCreationDto;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUp() {
        User owner = new User(1, "owner", "email@owner.com");
        requestor = new User(2, "requestor", "email@requestor.com");
        user = new User(3, "user", "email@user.com");
        ir = new ItemRequest(1, "desc", requestor, LocalDateTime.now());
        itemRequestCreationDto = new ItemRequestCreationDto(ir.getDescription());
        item = new Item(1, "name", "desc", true, owner, ir);
        ItemDtoForRequest itemDtoForRequest = new ItemDtoForRequest(item.getId(), item.getName(), item.getDescription(),
                item.getAvailable(), ir.getId());
        itemRequestDto = new ItemRequestDto(ir.getId(), ir.getDescription(), ir.getCreated(), List.of(itemDtoForRequest));
    }

    @Test
    @SneakyThrows
    void addRequest() {
        ItemRequestCreatedDto itemRequestCreatedDto = new ItemRequestCreatedDto(ir.getId(), ir.getDescription(),
                ir.getCreated());
        when(itemRequestService.addRequest(anyLong(), any())).thenReturn(itemRequestCreatedDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, requestor.getId())
                        .content(objectMapper.writeValueAsString(itemRequestCreationDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemRequestCreatedDto.getId()))
                .andExpect(jsonPath("$.description").value(itemRequestCreatedDto.getDescription()));
    }

    @Test
    @SneakyThrows
    void addRequest_whenDescriptionIsBlank_thenReturnBadRequest() {
        itemRequestCreationDto.setDescription("");

        mockMvc.perform(MockMvcRequestBuilders.post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, requestor.getId())
                        .content(objectMapper.writeValueAsString(itemRequestCreationDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).addRequest(anyLong(), any());
    }

    @Test
    @SneakyThrows
    void getListOwnRequests() {
        when(itemRequestService.getListOwnRequests(anyLong())).thenReturn(List.of(itemRequestDto));

        mockMvc.perform(get("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, requestor.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$[0].description").value(itemRequestDto.getDescription()))
                .andExpect(jsonPath("$[0].items").isArray())
                .andExpect(jsonPath("$[0].items[0].id").value(item.getId()))
                .andExpect(jsonPath("$[0].items[0].name").value(item.getName()))
                .andExpect(jsonPath("$[0].items[0].description").value(item.getDescription()))
                .andExpect(jsonPath("$[0].items[0].available").value(item.getAvailable()))
                .andExpect(jsonPath("$[0].items[0].requestId").value(item.getRequest().getId()));
    }

    @Test
    @SneakyThrows
    void getRequestById() {
        when(itemRequestService.getRequestById(anyLong(), anyLong())).thenReturn(itemRequestDto);

        mockMvc.perform(get("/requests/" + ir.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, user.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$.description").value(itemRequestDto.getDescription()))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].id").value(item.getId()))
                .andExpect(jsonPath("$.items[0].name").value(item.getName()))
                .andExpect(jsonPath("$.items[0].description").value(item.getDescription()))
                .andExpect(jsonPath("$.items[0].available").value(item.getAvailable()))
                .andExpect(jsonPath("$.items[0].requestId").value(item.getRequest().getId()));
    }

    @Test
    @SneakyThrows
    void getRequestsList() {
        when(itemRequestService.getRequestsList(anyLong(), anyInt(), anyInt())).thenReturn(List.of(itemRequestDto));

        mockMvc.perform(get("/requests/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, user.getId())
                        .param("from", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$[0].description").value(itemRequestDto.getDescription()))
                .andExpect(jsonPath("$[0].items").isArray())
                .andExpect(jsonPath("$[0].items[0].id").value(item.getId()))
                .andExpect(jsonPath("$[0].items[0].name").value(item.getName()))
                .andExpect(jsonPath("$[0].items[0].description").value(item.getDescription()))
                .andExpect(jsonPath("$[0].items[0].available").value(item.getAvailable()))
                .andExpect(jsonPath("$[0].items[0].requestId").value(item.getRequest().getId()));
    }

    @Test
    @SneakyThrows
    void getRequestsList_whenFromParamIsLessThanZero_whenReturnBadRequest() {
        mockMvc.perform(get("/requests/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, user.getId())
                        .param("from", "-1")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).getRequestsList(anyLong(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void getRequestsList_whenSizeParamIsNotPositive_whenReturnBadRequest() {
        mockMvc.perform(get("/requests/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, user.getId())
                        .param("from", "0")
                        .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).getRequestsList(anyLong(), anyInt(), anyInt());
    }
}