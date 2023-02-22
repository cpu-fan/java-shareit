package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@FieldDefaults(makeFinal = false)
public class UserDto {

    long id;

    @NotBlank
    String name;

    @NotBlank
    @Email
    String email;
}
