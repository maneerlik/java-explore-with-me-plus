package ru.practicum.dto.user.in;

import java.util.Collection;

public record GetUsersRequest(
        Collection<Long> ids,
        int from,
        int size
) {
}
