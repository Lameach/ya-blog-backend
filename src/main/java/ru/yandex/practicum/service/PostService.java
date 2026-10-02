package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.PostCreateDto;
import ru.yandex.practicum.dto.PostDto;
import ru.yandex.practicum.dto.PostPageDto;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.model.Tag;
import ru.yandex.practicum.repository.PostRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public PostDto createPost(PostCreateDto createDto) {
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
        String searchTerm = search == null ? "" : search;

        int offset = (pageNumber - 1) * pageSize;

        List<Post> posts = postRepository.searchPosts(searchTerm, pageSize, offset);
        int totalPosts = postRepository.countPosts(searchTerm);

        int lastPage = (int) Math.ceil((double) totalPosts / pageSize);
        if (lastPage == 0) lastPage = 1;

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
            throw new RuntimeException("Пост с id " + id + " не найден");
        }
        return mapToDto(post.get());
    }

    public String getImageFileNameById(Long id) {
        return postRepository.getImageFileNameById(id);
    }

    public void updateImageName(Long id, String imageName) {
        postRepository.updateImageName(id, imageName);
    }

    private PostDto mapToDto (Post post) {
        PostDto postDto = new PostDto();
        postDto.setId(post.getId());
        postDto.setTitle(post.getTitle());
        postDto.setText(post.getText());
        postDto.setLikesCount(post.getLikesCount());
        postDto.setCommentsCount(post.getCommentsCount());
        postDto.setTags(post.getTags().stream().map(Tag::getTag).toList());
        return postDto;
    }
}