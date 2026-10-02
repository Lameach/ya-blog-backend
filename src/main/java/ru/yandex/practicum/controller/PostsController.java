package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.dto.PostCreateDto;
import ru.yandex.practicum.dto.PostDto;
import ru.yandex.practicum.service.FilesService;
import ru.yandex.practicum.service.PostService;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostsController {

    private final PostService postService;
    private final FilesService filesService;

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


    @PostMapping
    public PostDto createPost(@RequestBody PostCreateDto newPost) {
        return postService.createPost(newPost);
    }
}
