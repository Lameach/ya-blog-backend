package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.service.CommentService;
import ru.yandex.practicum.service.FilesService;
import ru.yandex.practicum.service.PostService;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostsController {

    private final PostService postService;
    private final FilesService filesService;
    private final CommentService commentService;

    @GetMapping
    public Object getPosts(
            @RequestParam String search,
            @RequestParam int pageNumber,
            @RequestParam int pageSize) {
        return postService.getPosts(search, pageNumber, pageSize);
    }

    @GetMapping("/{id}")
    public Object getPost(
            @PathVariable("id") long id) {
        return postService.getPostById(id);
    }

    @PutMapping("/{id}")
    public Object getPost(@RequestBody PostEditDto editedPost) {
        return postService.editPost(editedPost);
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable("id") Long id) {
        postService.deletePost(id);
    }

    @PostMapping("/{id}/likes")
    public Integer incrementLikesCount(@PathVariable("id") Long id) {
        return postService.incrementLikesCount(id);
    }

    @PutMapping("/{id}/image")
    public String uploadFile(@RequestParam("image") MultipartFile image, @PathVariable("id") Long id) {
        String fileName = filesService.upload(image);
        postService.updateImageName(id, fileName);
        return fileName;
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
        String filename = postService.getImageFileNameById(id);
        Resource file = filesService.download(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

    @GetMapping("/{postId}/comments")
    public List<CommentDto> getAllComments(@PathVariable("postId") Long postId) {
        return commentService.getAllComments(postId);
    }

    @PostMapping("/{postId}/comments")
    public CommentDto createComment(@RequestBody CommentCreateDto newComment) {
        return commentService.createComment(newComment);
    }

    @PutMapping("/{postId}/comments/{id}")
    public CommentDto createComment(@RequestBody CommentDto editComment) {
        return commentService.editComment(editComment);
    }

    @DeleteMapping("/{postId}/comments/{id}")
    public void createComment(@PathVariable("id") Long id) {
        commentService.deleteComment(id);
    }


    @PostMapping
    public PostDto createPost(@RequestBody PostCreateDto newPost) {
        return postService.createPost(newPost);
    }
}
