package ru.practicum.service.user;

import ru.practicum.dto.user.UserDTO.Request.NewUserRequest;
import ru.practicum.dto.user.UserDTO.Response.UserDto;
import ru.practicum.dto.user.in.GetUsersRequest;

import java.util.Collection;

public interface UserService {
    UserDto createUser(NewUserRequest requestDto);

    Collection<UserDto> getUsers(GetUsersRequest request);

    UserDTO.Response.UserShortDto getUser(Long userId);

    void deleteUser(Long id);
}
