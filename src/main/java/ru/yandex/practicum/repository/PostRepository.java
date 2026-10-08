package ru.yandex.practicum.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.Post;

import java.util.List;

public interface PostRepository extends CrudRepository<Post, Long>, PagingAndSortingRepository<Post, Long> {

    @Query("""
            SELECT id FROM posts
            WHERE (:title = '' OR title ILIKE '%' || :title || '%')
            ORDER BY id DESC
            LIMIT :limit OFFSET :offset
            """)
    List<Long> findIdsByTitle(@Param("title") String title,
                              @Param("limit") int limit,
                              @Param("offset") int offset);

    @Query("""
            SELECT COUNT(*) FROM posts
            WHERE (:title = '' OR title ILIKE '%' || :title || '%')
            """)
    int countByTitle(@Param("title") String title);

    @Query("""
            SELECT id FROM posts
            WHERE (:title = '' OR title ILIKE '%' || :title || '%')
              AND (
                SELECT COUNT(DISTINCT LOWER(tag)) FROM tags
                WHERE post_id = posts.id AND LOWER(tag) IN (:tags)
              ) = :tagCount
            ORDER BY id DESC
            LIMIT :limit OFFSET :offset
            """)
    List<Long> findIdsByTitleAndTags(@Param("title") String title,
                                     @Param("tags") List<String> tags,
                                     @Param("tagCount") int tagCount,
                                     @Param("limit") int limit,
                                     @Param("offset") int offset);

    @Query("""
            SELECT COUNT(*) FROM posts
            WHERE (:title = '' OR title ILIKE '%' || :title || '%')
              AND (
                SELECT COUNT(DISTINCT LOWER(tag)) FROM tags
                WHERE post_id = posts.id AND LOWER(tag) IN (:tags)
              ) = :tagCount
            """)
    int countByTitleAndTags(@Param("title") String title,
                            @Param("tags") List<String> tags,
                            @Param("tagCount") int tagCount);

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
