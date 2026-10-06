package ru.yandex.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.yandex.practicum.BaseIntegrationTest;
import ru.yandex.practicum.dto.CommentCreateDto;
import ru.yandex.practicum.dto.PostCreateDto;
import ru.yandex.practicum.dto.PostDto;
import ru.yandex.practicum.dto.PostEditDto;
import ru.yandex.practicum.service.FilesService;
import ru.yandex.practicum.service.PostService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PostsControllerTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PostService postService;

    @Autowired
    private FilesService filesService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
        Mockito.reset(filesService);
    }

    @Test
    void shouldCreatePost_MvcAndDaoTest() throws Exception {
        PostCreateDto newPost = new PostCreateDto();
        newPost.setTitle("MVC Title");
        newPost.setText("MVC Text");
        newPost.setTags(List.of("spring"));

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPost)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("MVC Title"))
                .andExpect(jsonPath("$.likesCount").value(0));

        assertEquals(1, postService.getPosts("MVC Title", 1, 5).getPosts().size());
    }

    @Test
    void shouldDeletePost_MvcAndDaoTest() throws Exception {
        PostCreateDto newPost = new PostCreateDto();
        newPost.setTitle("Post to delete");
        newPost.setText("Text");
        PostDto savedPost = postService.createPost(newPost);

        mockMvc.perform(delete("/api/posts/{id}", savedPost.getId()))
                .andExpect(status().isOk());

        assertThrows(RuntimeException.class, () -> postService.getPostById(savedPost.getId()));
    }

    @Test
    void shouldFilterPosts_MvcAndDaoTest() throws Exception {
        PostCreateDto targetPost = new PostCreateDto();
        targetPost.setTitle("Java Spring Boot");
        targetPost.setText("Content");
        postService.createPost(targetPost);

        PostCreateDto noisePost = new PostCreateDto();
        noisePost.setTitle("Frontend Vue JS");
        noisePost.setText("Content");
        postService.createPost(noisePost);

        mockMvc.perform(get("/api/posts")
                        .param("search", "Spring")
                        .param("pageNumber", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()").value(1))
                .andExpect(jsonPath("$.posts[0].title").value("Java Spring Boot"));
    }

    @Test
    void shouldEditPost() throws Exception {
        PostCreateDto createDto = new PostCreateDto();
        createDto.setTitle("Old Title");
        createDto.setText("Old Text");
        PostDto savedPost = postService.createPost(createDto);

        PostEditDto editDto = new PostEditDto();
        editDto.setId(savedPost.getId());
        editDto.setTitle("New Title");
        editDto.setText("New Text");
        editDto.setTags(List.of("updated"));

        mockMvc.perform(put("/api/posts/{id}", savedPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.tags[0]").value("updated"));
    }

    @Test
    void shouldIncrementLikes() throws Exception {
        PostCreateDto createDto = new PostCreateDto();
        createDto.setTitle("Post for likes");
        createDto.setText("Text");
        PostDto savedPost = postService.createPost(createDto);

        mockMvc.perform(post("/api/posts/{id}/likes", savedPost.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("1")); // Ожидаем ответ "1"
    }

    @Test
    void shouldUploadImageViaPut() throws Exception {
        PostCreateDto createDto = new PostCreateDto();
        createDto.setTitle("Image Post");
        createDto.setText("Text");
        PostDto savedPost = postService.createPost(createDto);

        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy image content".getBytes()
        );

        Mockito.when(filesService.upload(Mockito.any())).thenReturn("uuid_test.jpg");

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/posts/{id}/image", savedPost.getId())
                        .file(file)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().string("uuid_test.jpg"));
    }

    @Test
    void shouldReturnNotFoundForMissingPost() throws Exception {
        mockMvc.perform(get("/api/posts/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestForInvalidPage() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("search", "")
                        .param("pageNumber", "0")
                        .param("pageSize", "5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateCommentAndReturnJson() throws Exception {
        PostCreateDto createDto = new PostCreateDto();
        createDto.setTitle("Post for comments");
        createDto.setText("Text");
        PostDto savedPost = postService.createPost(createDto);

        CommentCreateDto commentDto = new CommentCreateDto();
        commentDto.setPostId(savedPost.getId());
        commentDto.setText("My new comment");

        mockMvc.perform(post("/api/posts/{postId}/comments", savedPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").value("My new comment"))
                .andExpect(jsonPath("$.postId").value(savedPost.getId()));
    }
}