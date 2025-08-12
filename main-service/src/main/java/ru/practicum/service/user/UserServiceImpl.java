package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.in.GetUsersRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;


    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        log.info("Creating user {}", newUserRequest);

        boolean emailExists = userRepository.existsByEmail(newUserRequest.getEmail());
        if (emailExists) {
            log.warn("User creation rejected: email '{}' is already exist", newUserRequest.getEmail());
            throw new ConflictException(String.format("Email exists: %s", newUserRequest.getEmail()));
        }

        User user = User.builder()
                .name(newUserRequest.getName())
                .email(newUserRequest.getEmail())
                .build();
        User savedUser = userRepository.save(user);

        log.info("User created with id={}", savedUser.getId());

        return UserMapper.toFullDto(savedUser);
    }

    public Collection<UserDto> getUsers(GetUsersRequest request) {
        Collection<Long> ids = request.ids();
        int from = request.from();
        int size = request.size();

        if (size <= 0) return List.of();

        log.info("Getting users by ids={}, from={}, size={}", ids, from, size);

        Pageable pageable = PageRequest.of(from / size, size);

        List<User> users;

        if (ids == null) {
            users = userRepository.findAll(pageable).getContent();
        } else if (ids.isEmpty()) {
            users = List.of();
        } else {
            users = userRepository.findByIdIn(ids, pageable);
        }

        log.debug("Found {} users", users.size());

        return users.stream()
                .map(UserMapper::toFullDto)
                .toList();
    }

    public UserDto getUser(Long userId) {
        checkExistUser(userId);
        return UserMapper.toUserDto(userRepository.findById(userId).get());
    }

    public User getUserEntity(Long userId) {
        checkExistUser(userId);
        return userRepository.findById(userId).get();
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Delete user with id={}", userId);
        checkExistUser(userId);

        try {
            userRepository.deleteById(userId);
            log.info("User with id={} deleted", userId);
        } catch (Exception e) {
            log.error("Failed to delete user with id={}. Possible database error", userId, e);
            throw new ConflictException("Could not delete user");
        }
    }

    private void checkExistUser(Long userId) {
        boolean userExists = userRepository.existsById(userId);

        if (!userExists) {
            log.warn("User with id={} not found", userId);
            throw new NotFoundException(String.format("User with id: %s not found", userId));
        }
    }
}
