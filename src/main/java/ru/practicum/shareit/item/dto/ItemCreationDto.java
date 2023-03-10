package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@FieldDefaults(makeFinal = false)
public class ItemCreationDto {

    long id;

    @NotBlank
    String name;

    @NotBlank
    String description;

    @NotNull
    Boolean available;
}
