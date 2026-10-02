package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.CommentCreateDto;
import ru.yandex.practicum.dto.CommentDto;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.repository.CommentRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    public List<CommentDto> getAllComments(Long postId) {
        return commentRepository.getAllCommentsByPostId(postId).stream().map(this::mapToDto).toList();
    }

    @Transactional
    public CommentDto createComment(CommentCreateDto createDto) {
        Comment comment = new Comment();
        comment.setText(createDto.getText());
        comment.setPostId(createDto.getPostId());

        Comment savedComment = commentRepository.save(comment);
        return mapToDto(savedComment);
    }

    @Transactional
    public CommentDto editComment(CommentDto editDto) {
        Optional<Comment> optionalComment = commentRepository.findById(editDto.getId());
        if (optionalComment.isEmpty()) {
            throw new RuntimeException("Пост с id " + editDto.getId() + " не найден");
        }
        Comment comment = optionalComment.get();

        comment.setText(editDto.getText());
        Comment savedComment = commentRepository.save(comment);
        return mapToDto(savedComment);
    }

    @Transactional
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    private CommentDto mapToDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setPostId(comment.getPostId());
        return commentDto;
    }
}
