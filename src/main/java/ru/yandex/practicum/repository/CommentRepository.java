package ru.yandex.practicum.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.Comment;

import java.util.List;

public interface CommentRepository extends CrudRepository<Comment, Long> {
    @Query("SELECT * FROM comments WHERE post_id = :postId")
    List<Comment> getAllCommentsByPostId(@Param("postId") Long postId);
}
