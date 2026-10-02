package ru.yandex.practicum.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostEditDto {
    private Long id;
    private String title;
    private String text;
    private List<String> tags;
}