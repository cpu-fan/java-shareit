package ru.practicum.shareit.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class Mapper {

    public Map<String, Object> toMap(Object shareItObject) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> objectFields = mapper.convertValue(shareItObject, new TypeReference<>() {});
        return objectFields.entrySet()
                .stream()
                .filter(k -> k.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
