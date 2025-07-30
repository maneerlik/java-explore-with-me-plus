package ru.practicum.service.user;

import ru.practicum.dto.user.UserDTO;
import ru.practicum.dto.user.in.GetUsersRequest;

import java.util.Collection;

public interface UserService {
    UserDTO.Response.Full createUser(UserDTO.Request.Create requestDto);

    Collection<UserDTO.Response.Full> getUsers(GetUsersRequest request);

    void deleteUser(Long id);
}
