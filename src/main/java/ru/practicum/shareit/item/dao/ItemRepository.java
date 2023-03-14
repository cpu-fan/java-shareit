package ru.practicum.shareit.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query(value = "select * " +
            "from items i " +
            "where (i.name ilike ?1 " +
            "or i.description ilike ?1) " +
            "and is_available is true", nativeQuery = true)
    List<Item> search(String text);

    List<Item> findByOwnerId(long ownerId);
}
