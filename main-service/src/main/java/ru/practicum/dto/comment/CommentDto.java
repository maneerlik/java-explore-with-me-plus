package ru.practicum.dto.comment;

import lombok.Builder;
import lombok.Data;
import ru.practicum.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private Long id;
    private String text;
    private Long eventId;
    private UserShortDto author;
    private LocalDateTime createdOn;
}