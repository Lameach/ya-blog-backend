package ru.yandex.practicum.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.HashSet;
import java.util.Set;

@Data
@Table("posts")
public class Post {

    @Id
    private Long id;

    private String title;
    private String text;
    private Integer likesCount;
    private Integer commentsCount;

    @MappedCollection(idColumn = "post_id")
    private Set<Tag> tags = new HashSet<>();

    public void addTag(String tagValue) {
        this.tags.add(new Tag(tagValue));
    }
}