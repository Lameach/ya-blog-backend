package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.dto.PostCreateDto;
import ru.yandex.practicum.dto.PostDto;
import ru.yandex.practicum.dto.PostEditDto;
import ru.yandex.practicum.dto.PostPageDto;
import ru.yandex.practicum.exception.BadRequestException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.model.Tag;
import ru.yandex.practicum.repository.PostRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final FilesService filesService;

    @Transactional
    public PostDto createPost(PostCreateDto createDto) {
        requireText(createDto.getTitle(), createDto.getText());

        Post post = new Post();
        post.setTitle(createDto.getTitle());
        post.setText(createDto.getText());
        post.setLikesCount(0);
        post.setCommentsCount(0);

        if (createDto.getTags() != null) {
            createDto.getTags().forEach(post::addTag);
        }

        Post savedPost = postRepository.save(post);
        return mapToDto(savedPost);
    }

    public PostPageDto getPosts(String search, int pageNumber, int pageSize) {
        if (pageNumber < 1 || pageSize < 1) {
            throw new BadRequestException("Номер и размер страницы должны быть больше нуля");
        }

        ParsedSearch parsed = parseSearch(search);
        int offset = (pageNumber - 1) * pageSize;

        List<Long> ids;
        int totalPosts;
        if (parsed.tags().isEmpty()) {
            ids = postRepository.findIdsByTitle(parsed.title(), pageSize, offset);
            totalPosts = postRepository.countByTitle(parsed.title());
        } else {
            ids = postRepository.findIdsByTitleAndTags(
                    parsed.title(), parsed.tags(), parsed.tags().size(), pageSize, offset);
            totalPosts = postRepository.countByTitleAndTags(
                    parsed.title(), parsed.tags(), parsed.tags().size());
        }

        Map<Long, Post> postsById = new HashMap<>();
        postRepository.findAllById(ids).forEach(post -> postsById.put(post.getId(), post));
        List<Post> posts = ids.stream()
                .map(postsById::get)
                .filter(Objects::nonNull)
                .toList();

        int lastPage = (int) Math.ceil((double) totalPosts / pageSize);
        if (lastPage == 0) {
            lastPage = 1;
        }

        List<PostDto> postDtos = posts.stream()
                .map(post -> {
                    PostDto dto = mapToDto(post);
                    dto.setText(truncateText(dto.getText()));
                    return dto;
                })
                .toList();

        PostPageDto pageDto = new PostPageDto();
        pageDto.setPosts(postDtos);
        pageDto.setLastPage(lastPage);
        pageDto.setHasNext(pageNumber < lastPage);
        pageDto.setHasPrev(pageNumber > 1);
        return pageDto;
    }

    private String truncateText(String text) {
        if (text != null && text.length() > 128) {
            return text.substring(0, 128) + "…";
        }
        return text;
    }

    public PostDto getPostById(Long id) {
        Optional<Post> post = postRepository.findById(id);
        if (post.isEmpty()) {
            throw new NotFoundException("Пост с id " + id + " не найден");
        }
        return mapToDto(post.get());
    }

    @Transactional
    public PostDto editPost(PostEditDto editDto) {
        requireText(editDto.getTitle(), editDto.getText());
        Optional<Post> optionalPost = postRepository.findById(editDto.getId());
        if (optionalPost.isEmpty()) {
            throw new NotFoundException("Пост с id " + editDto.getId() + " не найден");
        }
        Post post = optionalPost.get();

        post.setTitle(editDto.getTitle());
        post.setText(editDto.getText());

        post.getTags().clear();
        if (editDto.getTags() != null) {
            editDto.getTags().forEach(post::addTag);
        }
        return mapToDto(postRepository.save(post));
    }

    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new NotFoundException("Пост с id " + id + " не найден");
        }
        String imageName = postRepository.getImageFileNameById(id);
        postRepository.deleteById(id);
        if (imageName != null) {
            filesService.deleteFile(imageName);
        }
    }

    @Transactional
    public Integer incrementLikesCount(Long id) {
        if (!postRepository.existsById(id)) {
            throw new NotFoundException("Пост с id " + id + " не найден");
        }
        postRepository.incrementLikesCount(id);
        return postRepository.findById(id).get().getLikesCount();
    }

    public String getImageFileNameById(Long id) {
        return postRepository.getImageFileNameById(id);
    }

    public String uploadImageForPost(MultipartFile image, Long id) {
        if (!postRepository.existsById(id)) {
            throw new NotFoundException("Пост с id " + id + " не найден");
        }
        String fileName = filesService.upload(image);
        this.updateImageName(id, fileName);
        return fileName;
    }

    @Transactional
    public void updateImageName(Long id, String newImageName) {
        String oldImageName = postRepository.getImageFileNameById(id);
        postRepository.updateImageName(id, newImageName);

        if (oldImageName != null && !oldImageName.equals(newImageName)) {
            filesService.deleteFile(oldImageName);
        }
    }

    public Resource downloadImageByPostId(Long id) {
        if (!postRepository.existsById(id)) {
            throw new NotFoundException("Пост с id " + id + " не найден");
        }
        String filename = this.getImageFileNameById(id);
        if (filename == null || filename.isBlank()) {
            throw new NotFoundException("У поста нет изображения");
        }
        return filesService.download(filename);
    }

    private void requireText(String title, String text) {
        if (title == null || title.isBlank() || text == null || text.isBlank()) {
            throw new BadRequestException("Заголовок и текст поста не могут быть пустыми");
        }
    }

    private ParsedSearch parseSearch(String search) {
        List<String> titleWords = new ArrayList<>();
        Set<String> tags = new LinkedHashSet<>();
        if (search != null && !search.isBlank()) {
            for (String word : search.trim().split("\\s+")) {
                if (word.isEmpty()) {
                    continue;
                }
                if (word.startsWith("#")) {
                    String tag = word.substring(1).trim();
                    if (!tag.isEmpty()) {
                        tags.add(tag.toLowerCase(Locale.ROOT));
                    }
                } else {
                    titleWords.add(word);
                }
            }
        }
        return new ParsedSearch(String.join(" ", titleWords), List.copyOf(tags));
    }

    private PostDto mapToDto(Post post) {
        PostDto postDto = new PostDto();
        postDto.setId(post.getId());
        postDto.setTitle(post.getTitle());
        postDto.setText(post.getText());
        postDto.setLikesCount(post.getLikesCount());
        postDto.setCommentsCount(post.getCommentsCount());
        Set<Tag> tags = post.getTags() == null ? Set.of() : post.getTags();
        postDto.setTags(tags.stream().map(Tag::getTag).toList());
        return postDto;
    }

    private record ParsedSearch(String title, List<String> tags) {
    }
}
