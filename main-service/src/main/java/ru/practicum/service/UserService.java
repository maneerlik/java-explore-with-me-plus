package ru.practicum.service;

import ru.practicum.dto.UserDTO;
import ru.practicum.dto.in.GetUsersRequest;

import java.util.Collection;

public interface UserService {
    UserDTO.Response.Full createUser(UserDTO.Request.Create requestDto);

    Collection<UserDTO.Response.Full> getUsers(GetUsersRequest request);

    void deleteUser(Long id);
}
