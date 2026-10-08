package ru.yandex.practicum.dto;

import lombok.Data;

@Data
public class CommentDto {
    private Long id;
    private String text;
    private Long postId;
}
