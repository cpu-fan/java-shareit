package ru.practicum.shareit.item.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select i " +
            "from Item i " +
            "where (lower(i.name) like lower(concat('%', ?1, '%')) " +
            "or lower(i.description) like lower(concat('%', ?1, '%'))) " +
            "and i.available is true")
    List<Item> search(String text, Pageable pageable);

    List<Item> findByOwnerId(long ownerId, Pageable pageable);

    List<Item> findByRequestIdIn(List<Long> requestIds);

    List<Item> findByRequestId(long requestId);
}
