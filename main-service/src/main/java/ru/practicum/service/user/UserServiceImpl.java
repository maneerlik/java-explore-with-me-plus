package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.UserDTO.Request.NewUserRequest;
import ru.practicum.dto.user.UserDTO.Response.UserDto;
import ru.practicum.dto.user.in.GetUsersRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.Collection;
import java.util.List;

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
        List<Long> ids = request.ids().stream().toList();
        int from = request.from();
        int size = request.size();

        log.info("Getting users by ids={}, from={}, size={}", ids.isEmpty() ? "all" : ids, from, size);

        Pageable pageable = PageRequest.of(from / size, size);

        List<User> users = userRepository.findUsersByIds(ids, pageable);
        log.debug("Found {} users", users.size());

        return users.stream()
                .map(UserMapper::toFullDto)
                .toList();
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Delete user with id={}", userId);

        boolean userExists = userRepository.existsById(userId);
        if (!userExists) {
            log.warn("User deletion failed: user with id={} not found", userId);
            throw new NotFoundException(String.format("User with id: %s not found", userId));
        }

        try {
            userRepository.deleteById(userId);
            log.info("User with id={} deleted", userId);
        } catch (Exception e) {
            log.error("Failed to delete user with id={}. Possible database error", userId, e);
            throw new ConflictException("Could not delete user");
        }
    }
}
