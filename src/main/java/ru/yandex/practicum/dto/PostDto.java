package ru.yandex.practicum.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostDto {
    private Long id;

    private String title;
    private String text;
    private Integer likesCount;
    private Integer commentsCount;
    private List<String> tags;
}
