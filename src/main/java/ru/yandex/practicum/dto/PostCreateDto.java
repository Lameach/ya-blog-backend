package ru.yandex.practicum.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostCreateDto {
    private String title;
    private String text;
    private List<String> tags;
}