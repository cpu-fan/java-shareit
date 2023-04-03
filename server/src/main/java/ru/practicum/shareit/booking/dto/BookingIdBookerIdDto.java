package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingIdBookerIdDto {

    private long id;

    private long bookerId;
}
