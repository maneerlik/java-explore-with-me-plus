package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.UserDTO;
import ru.practicum.dto.in.GetUsersRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.UserService;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;


    @Transactional
    public UserDTO.Response.Full createUser(UserDTO.Request.Create newUserRequest) {
        boolean emailExists = userRepository.existsByEmail(newUserRequest.getEmail());
        if (emailExists) throw new ConflictException(String.format("Email exists: %s", newUserRequest.getEmail()));

        User user = User.builder()
                .name(newUserRequest.getName())
                .email(newUserRequest.getEmail())
                .build();
        User savedUser = userRepository.save(user);

        return UserMapper.toFullDto(savedUser);
    }

    public Collection<UserDTO.Response.Full> getUsers(GetUsersRequest request) {
        List<Long> ids = request.ids().stream().toList();
        int from = request.from();
        int size = request.size();

        Pageable pageable = PageRequest.of(from / size, size);

        return userRepository.findUsersByIds(ids, pageable).stream()
                .map(UserMapper::toFullDto)
                .toList();
    }

    @Transactional
    public void deleteUser(Long userId) {
        boolean userExists = userRepository.existsById(userId);
        if (!userExists) throw new NotFoundException(String.format("User with id: %s not found", userId));

        try {
            userRepository.deleteById(userId);
        } catch (Exception e) {
            throw new ConflictException("Could not delete user");
        }
    }
}
