package ru.practicum.shareit.booking.controller;

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
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemIdNameDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserIdDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    private static final String HEADER_NAME = "X-Sharer-User-Id";

    private User owner;
    private User booker;
    private ItemRequest itemRequest;
    private Booking booking;
    private BookingCreationDto bookingCreationDto;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        owner = new User(1, "owner", "email@owner.com");
        User requestor = new User(2, "requestor", "email@requestor.com");
        booker = new User(3, "booker", "email@booker.com");
        Item item = new Item(1, "name", "desc", true, owner, itemRequest);
        bookingCreationDto = new BookingCreationDto(item.getId(), LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2));
        booking = new Booking(1, bookingCreationDto.getStart(), bookingCreationDto.getEnd(), item, booker,
                BookingStatus.WAITING);
        itemRequest = new ItemRequest(1, "desc", requestor, LocalDateTime.now());

        UserIdDto bookerDto = new UserIdDto(booker.getId());
        ItemIdNameDto itemIdNameDto = new ItemIdNameDto(item.getId(), item.getName());
        bookingDto = new BookingDto(booking.getId(), booking.getStart(), booking.getEnd(),
                booking.getStatus(), bookerDto, itemIdNameDto);
    }

    @Test
    @SneakyThrows
    void createBooking() {
        when(bookingService.createBooking(anyLong(), any())).thenReturn(bookingDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, booker.getId())
                        .content(objectMapper.writeValueAsString(bookingCreationDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId()))
                .andExpect(jsonPath("$.status").value(booking.getStatus().toString()))
                .andExpect(jsonPath("$.booker.id").value(booking.getBooker().getId()))
                .andExpect(jsonPath("$.item.id").value(booking.getItem().getId()))
                .andExpect(jsonPath("$.item.name").value(booking.getItem().getName()));
    }

    @Test
    @SneakyThrows
    void createBooking_whenStartIsPast_whenReturnBadRequest() {
        bookingCreationDto.setStart(LocalDateTime.now().minusDays(1));

        mockMvc.perform(MockMvcRequestBuilders.post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, booker.getId())
                        .content(objectMapper.writeValueAsString(bookingCreationDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).createBooking(anyLong(), any());
    }

    @Test
    @SneakyThrows
    void createBooking_whenEndIsPast_whenReturnBadRequest() {
        bookingCreationDto.setEnd(LocalDateTime.now().minusDays(1));

        mockMvc.perform(MockMvcRequestBuilders.post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, booker.getId())
                        .content(objectMapper.writeValueAsString(bookingCreationDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).createBooking(anyLong(), any());
    }

    @Test
    @SneakyThrows
    void considerationOfRequest() {
        bookingDto.setStatus(BookingStatus.APPROVED);
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingService.considerationOfRequest(anyLong(), anyLong(), anyBoolean())).thenReturn(bookingDto);

        mockMvc.perform(MockMvcRequestBuilders.patch("/bookings/" + booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("approved", "true")
                        .header(HEADER_NAME, owner.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId()))
                .andExpect(jsonPath("$.status").value(booking.getStatus().toString()))
                .andExpect(jsonPath("$.booker.id").value(booking.getBooker().getId()))
                .andExpect(jsonPath("$.item.id").value(booking.getItem().getId()))
                .andExpect(jsonPath("$.item.name").value(booking.getItem().getName()));
    }

    @Test
    @SneakyThrows
    void considerationOfRequest_whenApprovedIsNotPresent_thenReturnStatusCode500() {
        mockMvc.perform(MockMvcRequestBuilders.patch("/bookings/" + booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, owner.getId()))
                .andDo(print())
                .andExpect(status().is5xxServerError());

        verify(bookingService, never()).considerationOfRequest(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    @SneakyThrows
    void getBookingById() {
        when(bookingService.getBookingById(anyLong(), anyLong())).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/" + booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, owner.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId()))
                .andExpect(jsonPath("$.status").value(booking.getStatus().toString()))
                .andExpect(jsonPath("$.booker.id").value(booking.getBooker().getId()))
                .andExpect(jsonPath("$.item.id").value(booking.getItem().getId()))
                .andExpect(jsonPath("$.item.name").value(booking.getItem().getName()));
    }

    @Test
    @SneakyThrows
    void getAllBookingsByUser() {
        when(bookingService.getAllBookingsByUser(anyLong(), any(), anyInt(), anyInt(), anyBoolean()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, booker.getId())
                        .param("state", "WAITING")
                        .param("from", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(booking.getId()))
                .andExpect(jsonPath("$[0].status").value(booking.getStatus().toString()))
                .andExpect(jsonPath("$[0].booker.id").value(booking.getBooker().getId()))
                .andExpect(jsonPath("$[0].item.id").value(booking.getItem().getId()))
                .andExpect(jsonPath("$[0].item.name").value(booking.getItem().getName()));
    }

    @Test
    @SneakyThrows
    void getAllBookingsByUser_whenParamsIsNotPresent_thenUseDefault() {
        mockMvc.perform(get("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, booker.getId()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getAllBookingsByUser(booker.getId(), null,
                0, 10, false);
    }

    @Test
    @SneakyThrows
    void getAllBookingsByUser_whenFromParamIsLessThanZero_whenReturnBadRequest() {
        mockMvc.perform(get("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, booker.getId())
                        .param("state", "ALL")
                        .param("from", "-1")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getAllBookingsByUser(anyLong(), any(), anyInt(), anyInt(), anyBoolean());
    }

    @Test
    @SneakyThrows
    void getAllBookingsByUser_whenSizeParamIsNotPositive_whenReturnBadRequest() {
        mockMvc.perform(get("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, booker.getId())
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getAllBookingsByUser(anyLong(), any(), anyInt(), anyInt(), anyBoolean());
    }

    @Test
    @SneakyThrows
    void getAllBookingsByOwner() {
        when(bookingService.getAllBookingsByUser(anyLong(), any(), anyInt(), anyInt(), anyBoolean()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, owner.getId())
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(booking.getId()))
                .andExpect(jsonPath("$[0].status").value(booking.getStatus().toString()))
                .andExpect(jsonPath("$[0].booker.id").value(booking.getBooker().getId()))
                .andExpect(jsonPath("$[0].item.id").value(booking.getItem().getId()))
                .andExpect(jsonPath("$[0].item.name").value(booking.getItem().getName()));
    }

    @Test
    @SneakyThrows
    void getAllBookingsByOwner_whenParamsIsNotPresent_thenUseDefault() {
        mockMvc.perform(get("/bookings/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, owner.getId()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getAllBookingsByUser(owner.getId(), null,
                0, 10, true);
    }

    @Test
    @SneakyThrows
    void getAllBookingsByUser_whenIsOwnerFromParamIsLessThanZero_whenReturnBadRequest() {
        mockMvc.perform(get("/bookings/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, booker.getId())
                        .param("state", "ALL")
                        .param("from", "-1")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getAllBookingsByUser(anyLong(), any(), anyInt(), anyInt(), anyBoolean());
    }

    @Test
    @SneakyThrows
    void getAllBookingsByUser_whenIsOwnerSizeParamIsNotPositive_whenReturnBadRequest() {
        mockMvc.perform(get("/bookings/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, booker.getId())
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getAllBookingsByUser(anyLong(), any(), anyInt(), anyInt(), anyBoolean());
    }
}