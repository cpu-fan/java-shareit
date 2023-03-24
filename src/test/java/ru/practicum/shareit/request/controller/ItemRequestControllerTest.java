package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreatedDto;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestServiceImpl itemRequestService;

    private final ItemRequestCreationDto itemRequestCreationDto = new ItemRequestCreationDto("description");
    private final ItemRequestCreatedDto itemRequestDto = new ItemRequestCreatedDto(1, "description", LocalDateTime.now());

    private static final String HEADER_NAME = "X-Sharer-User-Id";

    @SneakyThrows
    @Test
    void addRequest() {
        when(itemRequestService.addRequest(anyLong(), itemRequestCreationDto)).thenReturn(itemRequestDto);

        String result = mockMvc.perform(post("/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_NAME, anyLong())
                .content(objectMapper.writeValueAsString(itemRequestCreationDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemRequestDto), result);
    }
}