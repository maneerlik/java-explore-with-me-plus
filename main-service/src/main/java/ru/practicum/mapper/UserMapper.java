package ru.practicum.mapper;

import ru.practicum.dto.user.UserDTO.Request.NewUserRequest;
import ru.practicum.dto.user.UserDTO.Response.UserDto;
import ru.practicum.dto.user.UserDTO.Response.UserShortDto;
import ru.practicum.model.User;

public final class UserMapper {
    /**
     * Don't let anyone instantiate this class.
     */
    private UserMapper() {

    }


    public static UserDto toFullDto(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getName());
    }

    public static UserShortDto toShortDto(User user) {
        return new UserShortDto(user.getId(), user.getName());
    }

    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getName());
    }

    public static User toUser(NewUserRequest newUserRequest) {
        return User.builder()
                .email(newUserRequest.getEmail())
                .name(newUserRequest.getName())
                .build();
    }

    public static User toUser(UserDto user) {
        return User.builder()
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}
