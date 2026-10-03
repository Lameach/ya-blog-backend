package ru.yandex.practicum.dto;

import lombok.Data;
import java.util.List;

@Data
public class PostPageDto {
    private List<PostDto> posts;
    private boolean hasPrev;
    private boolean hasNext;
    private int lastPage;
}