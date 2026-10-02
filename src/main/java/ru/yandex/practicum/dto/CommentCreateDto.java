package ru.yandex.practicum.dto;

import lombok.Data;

@Data
public class CommentCreateDto {
    private String text;
    private Long postId;
}
