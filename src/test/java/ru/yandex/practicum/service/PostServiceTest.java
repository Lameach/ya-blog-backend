package ru.yandex.practicum.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.BaseIntegrationTest;
import ru.yandex.practicum.dto.PostCreateDto;
import ru.yandex.practicum.dto.PostDto;
import ru.yandex.practicum.dto.PostEditDto;
import ru.yandex.practicum.dto.PostPageDto;
import ru.yandex.practicum.repository.CommentRepository;
import ru.yandex.practicum.repository.PostRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostServiceTest extends BaseIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @AfterEach
    void deleteAllFromDB() {
        postRepository.deleteAll();
        commentRepository.deleteAll();
    }

    @Test
    void shouldCreatePostAndMapTagsCorrectly() {
        PostCreateDto createDto = new PostCreateDto();
        createDto.setTitle("Тестовый заголовок");
        createDto.setText("Текст тестового поста");
        createDto.setTags(List.of("java", "spring"));

        PostDto savedPost = postService.createPost(createDto);

        assertNotNull(savedPost.getId(), "ID поста должен сгенерироваться");
        assertEquals("Тестовый заголовок", savedPost.getTitle());
        assertEquals(0, savedPost.getLikesCount());
        assertTrue(savedPost.getTags().contains("java"), "Тег должен сохраниться");
    }

    @Test
    void shouldTruncateTextIfMoreThan128CharsInPagination() {
        String longText = "A".repeat(150);
        PostCreateDto createDto = new PostCreateDto();
        createDto.setTitle("Длинный пост");
        createDto.setText(longText);
        postService.createPost(createDto);

        PostPageDto page = postService.getPosts("", 1, 10);
        PostDto retrievedPost = page.getPosts().get(0);

        assertEquals(129, retrievedPost.getText().length());
        assertTrue(retrievedPost.getText().endsWith("…"), "Текст должен обрезаться многоточием");
    }

    @Test
    void shouldThrowExceptionWhenPostNotFound() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            postService.getPostById(9999L);
        });

        assertTrue(exception.getMessage().contains("не найден"));
    }

    @Test
    void shouldEditPostSuccessfully() {
        PostCreateDto createDto = new PostCreateDto();
        createDto.setTitle("Старый заголовок");
        createDto.setText("Старый текст");
        createDto.setTags(List.of("oldTag"));
        PostDto savedPost = postService.createPost(createDto);

        PostEditDto editDto = new PostEditDto();
        editDto.setId(savedPost.getId());
        editDto.setTitle("Новый заголовок");
        editDto.setText("Новый текст");
        editDto.setTags(List.of("newTag"));

        PostDto updatedPost = postService.editPost(editDto);

        assertEquals("Новый заголовок", updatedPost.getTitle());
        assertEquals("Новый текст", updatedPost.getText());
        assertTrue(updatedPost.getTags().contains("newTag"), "Новый тег должен сохраниться");
        assertFalse(updatedPost.getTags().contains("oldTag"), "Старый тег должен быть удален");
    }

    @Test
    void shouldDeletePostSuccessfully() {
        PostCreateDto createDto = new PostCreateDto();
        createDto.setTitle("Пост для удаления");
        createDto.setText("Текст");
        PostDto savedPost = postService.createPost(createDto);

        postService.deletePost(savedPost.getId());

        assertThrows(RuntimeException.class, () -> {
            postService.getPostById(savedPost.getId());
        });
    }

    @Test
    void shouldReturnCorrectPageWithSearch() {
        PostCreateDto post1 = new PostCreateDto();
        post1.setTitle("Spring Boot Test");
        post1.setText("Текст");
        postService.createPost(post1);

        PostCreateDto post2 = new PostCreateDto();
        post2.setTitle("Java Backend");
        post2.setText("Spring Framework");
        postService.createPost(post2);

        PostCreateDto post3 = new PostCreateDto();
        post3.setTitle("Python");
        post3.setText("Текст");
        postService.createPost(post3);

        PostPageDto page = postService.getPosts("Spring", 1, 10);

        assertEquals(1, page.getPosts().size(), "Ищется только название, не текст поста");
        assertEquals("Spring Boot Test", page.getPosts().get(0).getTitle());
        assertFalse(page.isHasNext(), "Следующей страницы быть не должно");
    }

    @Test
    void shouldReturnTagsInFeed() {
        PostCreateDto createDto = new PostCreateDto();
        createDto.setTitle("Пост с тегами");
        createDto.setText("Текст");
        createDto.setTags(List.of("java", "spring"));
        postService.createPost(createDto);

        PostPageDto page = postService.getPosts("", 1, 10);

        assertEquals(1, page.getPosts().size());
        assertEquals(2, page.getPosts().get(0).getTags().size());
        assertTrue(page.getPosts().get(0).getTags().containsAll(List.of("java", "spring")));
    }

    @Test
    void shouldFilterByTitleSubstringAndAllTags() {
        PostCreateDto match = new PostCreateDto();
        match.setTitle("Spring Boot");
        match.setText("нет тега в тексте");
        match.setTags(List.of("java", "spring"));
        postService.createPost(match);

        PostCreateDto titleOnly = new PostCreateDto();
        titleOnly.setTitle("Spring Data");
        titleOnly.setText("Текст");
        titleOnly.setTags(List.of("java"));
        postService.createPost(titleOnly);

        PostCreateDto tagsOnly = new PostCreateDto();
        tagsOnly.setTitle("Другое название");
        tagsOnly.setText("Spring");
        tagsOnly.setTags(List.of("java", "spring"));
        postService.createPost(tagsOnly);

        PostPageDto byTitleAndTag = postService.getPosts("Spring Boot #Java", 1, 10);
        assertEquals(1, byTitleAndTag.getPosts().size());
        assertEquals("Spring Boot", byTitleAndTag.getPosts().get(0).getTitle());

        PostPageDto byBothTags = postService.getPosts("#java #spring", 1, 10);
        assertEquals(2, byBothTags.getPosts().size());
        assertTrue(byBothTags.getPosts().stream().map(PostDto::getTitle).noneMatch("Spring Data"::equals));
    }

    @Test
    void shouldReturnCorrectPaginationFlags() {
        for (int i = 1; i <= 12; i++) {
            PostCreateDto createDto = new PostCreateDto();
            createDto.setTitle("Пост номер " + i);
            createDto.setText("Текст");
            postService.createPost(createDto);
        }

        PostPageDto page2 = postService.getPosts("", 2, 5);

        assertEquals(5, page2.getPosts().size(), "На 2-й странице должно быть 5 постов");
        assertEquals(3, page2.getLastPage(), "Последняя страница должна быть 3");
        assertTrue(page2.isHasPrev(), "У 2-й страницы должна быть предыдущая (1-я)");
        assertTrue(page2.isHasNext(), "У 2-й страницы должна быть следующая (3-я)");

        PostPageDto page3 = postService.getPosts("", 3, 5);
        assertEquals(2, page3.getPosts().size(), "На 3-й (последней) странице должно быть 2 поста");
        assertTrue(page3.isHasPrev(), "У 3-й страницы должна быть предыдущая");
        assertFalse(page3.isHasNext(), "У 3-й страницы НЕ должно быть следующей");
    }
}