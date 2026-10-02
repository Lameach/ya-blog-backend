package ru.yandex.practicum.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Data
@Table("comments")
public class Comment {

    @Id
    private Long id;

    private String text;
    private Long postId;
}