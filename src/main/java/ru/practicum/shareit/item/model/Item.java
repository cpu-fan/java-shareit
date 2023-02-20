package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class Item {

    private long id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    private Boolean available;

    @NotBlank
    private User owner;

    private ItemRequest request;
}
