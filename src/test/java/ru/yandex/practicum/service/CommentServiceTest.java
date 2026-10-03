package ru.yandex.practicum.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.BaseIntegrationTest;
import ru.yandex.practicum.dto.CommentCreateDto;
import ru.yandex.practicum.dto.CommentDto;
import ru.yandex.practicum.dto.PostCreateDto;
import ru.yandex.practicum.dto.PostDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentServiceTest extends BaseIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Test
    void shouldCreateCommentAndIncrementPostCounter() {
        PostCreateDto postDto = new PostCreateDto();
        postDto.setTitle("Пост для комментов");
        postDto.setText("Текст");
        PostDto savedPost = postService.createPost(postDto);

        CommentCreateDto commentDto = new CommentCreateDto();
        commentDto.setPostId(savedPost.getId());
        commentDto.setText("Первый комментарий!");

        CommentDto savedComment = commentService.createComment(commentDto);

        assertNotNull(savedComment.getId());
        assertEquals("Первый комментарий!", savedComment.getText());
        assertEquals(savedPost.getId(), savedComment.getPostId());

        PostDto updatedPost = postService.getPostById(savedPost.getId());
        assertEquals(1, updatedPost.getCommentsCount(), "Счетчик комментариев поста должен стать 1");
    }

    @Test
    void shouldDeleteCommentAndDecrementPostCounter() {
        PostCreateDto postDto = new PostCreateDto();
        postDto.setTitle("Пост");
        postDto.setText("Текст");
        PostDto savedPost = postService.createPost(postDto);

        CommentCreateDto commentDto = new CommentCreateDto();
        commentDto.setPostId(savedPost.getId());
        commentDto.setText("Удали меня");
        CommentDto savedComment = commentService.createComment(commentDto);

        commentService.deleteComment(savedComment.getId());

        PostDto updatedPost = postService.getPostById(savedPost.getId());
        assertEquals(0, updatedPost.getCommentsCount(), "Счетчик комментариев поста должен стать 0 после удаления");

        List<CommentDto> comments = commentService.getAllComments(savedPost.getId());
        assertTrue(comments.isEmpty(), "Список комментариев должен быть пуст");
    }

    @Test
    void shouldEditCommentSuccessfully() {
        PostCreateDto postDto = new PostCreateDto();
        postDto.setTitle("Пост");
        postDto.setText("Текст");
        PostDto savedPost = postService.createPost(postDto);

        CommentCreateDto commentDto = new CommentCreateDto();
        commentDto.setPostId(savedPost.getId());
        commentDto.setText("Старый текст");
        CommentDto savedComment = commentService.createComment(commentDto);

        savedComment.setText("Новый текст");
        CommentDto updatedComment = commentService.editComment(savedComment);

        assertEquals("Новый текст", updatedComment.getText());

        PostDto updatedPost = postService.getPostById(savedPost.getId());
        assertEquals(1, updatedPost.getCommentsCount());
    }

    @Test
    void shouldThrowExceptionWhenEditingNonExistentComment() {
        CommentDto fakeComment = new CommentDto();
        fakeComment.setId(999L);
        fakeComment.setText("Текст");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            commentService.editComment(fakeComment);
        });

        assertTrue(exception.getMessage().contains("не найден"));
    }
}