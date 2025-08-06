package ru.practicum.service.user;

import ru.practicum.dto.user.UserDTO;
import ru.practicum.dto.user.in.GetUsersRequest;

import java.util.Collection;

public interface UserService {
    UserDTO.Response.UserDto createUser(UserDTO.Request.NewUserRequest requestDto);

    Collection<UserDTO.Response.UserDto> getUsers(GetUsersRequest request);

    UserDTO.Response.UserShortDto getUser(Long userId);

    void deleteUser(Long id);
}
