package ru.practicum.dto.in;

import java.util.Collection;

public record GetUsersRequest(
        Collection<Long> ids,
        int from,
        int size
) {
}
