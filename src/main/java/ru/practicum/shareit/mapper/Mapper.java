package ru.practicum.shareit.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class Mapper {

    public ItemDto toDto(Item item) {
        // Можно ли по code style большие конструкторы таким образом собирать?
        // Выглядит вроде нагляднее... (решил попробовать в этом проекте без @Builder обойтись)
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable()
        );
    }

    public Item toItem(User user, ItemDto itemDto) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                user, null
        );
    }

    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public User toUser(UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getName(),
                userDto.getEmail()
        );
    }

    public Map<String, Object> toMap(Object shareItObject) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> objectFields = mapper.convertValue(shareItObject, new TypeReference<>() {});
        return objectFields = objectFields.entrySet()
                .stream()
                .filter(k -> k.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
