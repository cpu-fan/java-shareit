package ru.practicum.shareit.user.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * TODO Sprint add-controllers.
 */

@Data
public class User {

    @Positive
    private long id;

    @NotBlank
    @NotNull
    private String name;

    @Email
    private String email;
}
