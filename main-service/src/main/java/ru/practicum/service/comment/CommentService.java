package ru.practicum.service.comment;


import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    List<CommentDto> getCommentsByEvent(Long eventId);

    List<CommentDto> getCommentsByUser(Long userId);

    void deleteComment(Long userId, Long commentId);
}