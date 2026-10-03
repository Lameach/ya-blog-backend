package ru.yandex.practicum.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.Post;

import java.util.List;

public interface PostRepository extends CrudRepository<Post, Long>, PagingAndSortingRepository<Post, Long> {
    @Query("SELECT * FROM posts WHERE title ILIKE '%' || :search || '%' OR text ILIKE '%' || :search || '%' ORDER BY id DESC LIMIT :limit OFFSET :offset")
    List<Post> searchPosts(@Param("search") String search, @Param("limit") int limit, @Param("offset") int offset);

    @Query("SELECT count(*) FROM posts WHERE title ILIKE '%' || :search || '%' OR text ILIKE '%' || :search || '%'")
    int countPosts(@Param("search") String search);

    @Query("SELECT image_name FROM posts WHERE id = :id")
    String getImageFileNameById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE posts SET image_name = :imageName WHERE id = :id")
    void updateImageName(@Param("id") Long id, @Param("imageName") String imageName);

    @Modifying
    @Query("UPDATE posts SET likes_count = likes_count + 1 WHERE id = :id")
    void incrementLikesCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE posts SET comments_count = comments_count + 1 WHERE id = :id")
    void incrementCommentsCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE posts SET comments_count = comments_count - 1 WHERE id = :id")
    void decrementCommentsCount(@Param("id") Long id);
}