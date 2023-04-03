package ru.practicum.shareit.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByItemId(long itemId);

    @Query("select c from Comment c where c.item.id in ?1")
    List<Comment> findByItemsId(List<Long> itemsId);
}
