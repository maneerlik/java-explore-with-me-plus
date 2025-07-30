package ru.practicum.mapper;

import ru.practicum.dto.UserDTO;
import ru.practicum.model.User;

public final class UserMapper {
    /**
     * Don't let anyone instantiate this class.
     */
    private UserMapper() {

    }


    public static UserDTO.Response.Full toFullDto(User user) {
        return new UserDTO.Response.Full(user.getId(), user.getEmail(), user.getName());
    }

    public static UserDTO.Response.Short toShortDto(User user) {
        return new UserDTO.Response.Short(user.getId(), user.getName());
    }

    public static User toUser(UserDTO.Request.Create create) {
        return User.builder()
                .email(create.getEmail())
                .name(create.getName())
                .build();
    }
}
