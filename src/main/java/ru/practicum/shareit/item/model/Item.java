package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@FieldDefaults(makeFinal = false)
public class Item {

    long id;

    @NotBlank
    String name;

    @NotBlank
    String description;

    @NotBlank
    Boolean available;

    @NotBlank
    User owner;

    ItemRequest request;
}
