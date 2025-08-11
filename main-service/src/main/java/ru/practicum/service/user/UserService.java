package ru.practicum.service.user;

import ru.practicum.dto.user.UserDTO.Request.NewUserRequest;
import ru.practicum.dto.user.UserDTO.Response.UserDto;
import ru.practicum.dto.user.in.GetUsersRequest;
import ru.practicum.model.User;

import java.util.Collection;

public interface UserService {
    UserDto createUser(NewUserRequest requestDto);

    Collection<UserDto> getUsers(GetUsersRequest request);

    UserDto getUser(Long userId);

    User getUserEntity(Long userId);

    void deleteUser(Long id);
}
