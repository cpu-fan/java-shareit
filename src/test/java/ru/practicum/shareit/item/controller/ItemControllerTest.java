package ru.practicum.shareit.item.controller;

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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentTextDto;
import ru.practicum.shareit.item.dto.ItemCreationDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    private static final String HEADER_NAME = "X-Sharer-User-Id";

    private User owner;
    private User booker;
    private User user;
    private Item item;
    private Comment comment;
    private ItemCreationDto itemCreationDtoReq;
    private ItemCreationDto itemCreationDtoResp;
    private ItemRequest itemRequest;
    private CommentDto commentDtoResp;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        owner = new User(1, "owner", "email@owner.com");
        User requestor = new User(2, "requestor", "email@requestor.com");
        booker = new User(3, "booker", "email@booker.com");
        user = new User(4, "user", "email@user.com");
        itemRequest = new ItemRequest(1, "item request desc", requestor, LocalDateTime.now());
        item = new Item(1, "item name", "item desc", true, owner, itemRequest);
        comment = new Comment(1, "comment text", item, booker, LocalDateTime.now());
        itemCreationDtoReq = new ItemCreationDto(0, item.getName(), item.getDescription(), item.getAvailable());
        itemCreationDtoResp = new ItemCreationDto(item.getId(), item.getName(), item.getDescription(),
                item.getAvailable());
        commentDtoResp = new CommentDto(comment.getId(), comment.getText(), comment.getAuthor().getName(),
                comment.getCreated());
        itemDto = new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable(), null,
                null, List.of(commentDtoResp));
    }

    @Test
    @SneakyThrows
    void getAllItems() {
        when(itemService.getOwnerItems(anyLong(), anyInt(), anyInt())).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, user.getId())
                        .param("from", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemDto.getId()))
                .andExpect(jsonPath("$[0].description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$[0].available").value(itemDto.getAvailable()))
                .andExpect(jsonPath("$[0].lastBooking").value(itemDto.getLastBooking()))
                .andExpect(jsonPath("$[0].nextBooking").value(itemDto.getNextBooking()))
                .andExpect(jsonPath("$[0].comments").isArray())
                .andExpect(jsonPath("$[0].comments[0].id").value(comment.getId()))
                .andExpect(jsonPath("$[0].comments[0].text").value(comment.getText()))
                .andExpect(jsonPath("$[0].comments[0].authorName").value(comment.getAuthor().getName()));
    }

    @Test
    @SneakyThrows
    void getAllItems_whenFromParamIsLessThanZero_whenReturnBadRequest() {
        mockMvc.perform(get("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, user.getId())
                        .param("from", "-1")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(itemService, never()).getOwnerItems(anyLong(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void getAllItems_whenSizeParamIsNotPositive_whenReturnBadRequest() {
        mockMvc.perform(get("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, user.getId())
                        .param("from", "0")
                        .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(itemService, never()).getOwnerItems(anyLong(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void getItemById() {
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(itemDto);

        mockMvc.perform(get("/items/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, user.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()))
                .andExpect(jsonPath("$.lastBooking").value(itemDto.getLastBooking()))
                .andExpect(jsonPath("$.nextBooking").value(itemDto.getNextBooking()))
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments[0].id").value(comment.getId()))
                .andExpect(jsonPath("$.comments[0].text").value(comment.getText()))
                .andExpect(jsonPath("$.comments[0].authorName").value(comment.getAuthor().getName()));
    }

    @Test
    @SneakyThrows
    void createItem() {
        when(itemService.createItem(anyLong(), any())).thenReturn(itemCreationDtoResp);

        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, owner.getId())
                        .content(objectMapper.writeValueAsString(itemCreationDtoReq)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemCreationDtoResp.getId()))
                .andExpect(jsonPath("$.name").value(itemCreationDtoResp.getName()))
                .andExpect(jsonPath("$.description").value(itemCreationDtoResp.getDescription()))
                .andExpect(jsonPath("$.available").value(itemCreationDtoResp.getAvailable()))
                .andExpect(jsonPath("$.requestId").value(itemCreationDtoResp.getRequestId()));
    }

    @Test
    @SneakyThrows
    void createItem_whenRequestIdIsExist_thenReturnItemWithRequestId() {
        itemCreationDtoReq.setRequestId(itemRequest.getId());
        when(itemService.createItem(anyLong(), any())).thenReturn(itemCreationDtoResp);

        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, owner.getId())
                        .content(objectMapper.writeValueAsString(itemCreationDtoReq)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemCreationDtoResp.getId()))
                .andExpect(jsonPath("$.name").value(itemCreationDtoResp.getName()))
                .andExpect(jsonPath("$.description").value(itemCreationDtoResp.getDescription()))
                .andExpect(jsonPath("$.available").value(itemCreationDtoResp.getAvailable()))
                .andExpect(jsonPath("$.requestId").value(itemCreationDtoResp.getRequestId()));
    }

    @Test
    @SneakyThrows
    void createItem_whenNameIsBlank_thenReturnBadRequest() {
        itemCreationDtoReq.setName("");

        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, owner.getId())
                        .content(objectMapper.writeValueAsString(itemCreationDtoReq)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(itemService, never()).createItem(anyLong(), any());
    }

    @Test
    @SneakyThrows
    void createItem_whenDescriptionIsBlank_thenReturnBadRequest() {
        itemCreationDtoReq.setDescription("");

        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, owner.getId())
                        .content(objectMapper.writeValueAsString(itemCreationDtoReq)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(itemService, never()).createItem(anyLong(), any());
    }

    @Test
    @SneakyThrows
    void createItem_whenAvailableIsNull_thenReturnBadRequest() {
        itemCreationDtoReq.setAvailable(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, owner.getId())
                        .content(objectMapper.writeValueAsString(itemCreationDtoReq)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(itemService, never()).createItem(anyLong(), any());
    }

    @Test
    @SneakyThrows
    void updateItem_whenUpdatedName_thenReturnItemWithUpdatedName() {
        String newName = "new name for itemId " + item.getId();
        itemCreationDtoReq.setName(newName);
        itemCreationDtoResp.setName(newName);
        when(itemService.updateItem(anyLong(), anyLong(), any())).thenReturn(itemCreationDtoResp);

        mockMvc.perform(MockMvcRequestBuilders.patch("/items/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, owner.getId())
                        .content(objectMapper.writeValueAsString(itemCreationDtoReq)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemCreationDtoResp.getId()))
                .andExpect(jsonPath("$.name").value(newName))
                .andExpect(jsonPath("$.description").value(itemCreationDtoResp.getDescription()))
                .andExpect(jsonPath("$.available").value(itemCreationDtoResp.getAvailable()))
                .andExpect(jsonPath("$.requestId").value(itemCreationDtoResp.getRequestId()));
    }

    @Test
    @SneakyThrows
    void searchItem() {
        when(itemService.searchItem(any(), anyInt(), anyInt())).thenReturn(List.of(itemCreationDtoResp));

        mockMvc.perform(get("/items/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, user.getId())
                        .param("text", "item name")
                        .param("from", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(item.getId()))
                .andExpect(jsonPath("$[0].name").value(item.getName()))
                .andExpect(jsonPath("$[0].description").value(item.getDescription()))
                .andExpect(jsonPath("$[0].available").value(item.getAvailable()));
    }

    @Test
    @SneakyThrows
    void searchItem_whenFromParamIsLessThanZero_whenReturnBadRequest() {
        mockMvc.perform(get("/items/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, user.getId())
                        .param("text", "item name")
                        .param("from", "-1")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(itemService, never()).searchItem("item name", -1, 10);
    }

    @Test
    @SneakyThrows
    void searchItem_whenSizeParamIsNotPositive_whenReturnBadRequest() {
        mockMvc.perform(get("/items/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, user.getId())
                        .param("text", "item name")
                        .param("from", "0")
                        .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(itemService, never()).searchItem("item name", 0, 0);
    }

    @Test
    @SneakyThrows
    void addComment() {
        CommentTextDto commentTextDto = new CommentTextDto(comment.getText());
        when(itemService.addComment(anyLong(), anyLong(), any())).thenReturn(commentDtoResp);

        mockMvc.perform(MockMvcRequestBuilders.post("/items/" + item.getId() + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, booker.getId())
                        .content(objectMapper.writeValueAsString(commentTextDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comment.getId()))
                .andExpect(jsonPath("$.text").value(comment.getText()))
                .andExpect(jsonPath("$.authorName").value(comment.getAuthor().getName()));

        verify(itemService, times(1)).addComment(booker.getId(), item.getId(), commentTextDto);
    }

    @Test
    @SneakyThrows
    void addComment_whenCommentTextIsBlank_thenReturnBadRequest() {
        CommentTextDto commentTextDto = new CommentTextDto("");

        mockMvc.perform(MockMvcRequestBuilders.post("/items/" + item.getId() + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, booker.getId())
                        .content(objectMapper.writeValueAsString(commentTextDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(itemService, never()).addComment(booker.getId(), item.getId(), commentTextDto);
    }
}