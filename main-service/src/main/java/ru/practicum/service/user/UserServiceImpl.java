package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.UserDTO;
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
    public UserDTO.Response.UserDto createUser(UserDTO.Request.NewUserRequest newUserRequest) {
        boolean emailExists = userRepository.existsByEmail(newUserRequest.getEmail());
        if (emailExists) throw new ConflictException(String.format("Email exists: %s", newUserRequest.getEmail()));

        User user = User.builder()
                .name(newUserRequest.getName())
                .email(newUserRequest.getEmail())
                .build();
        User savedUser = userRepository.save(user);

        return UserMapper.toFullDto(savedUser);
    }

    public Collection<UserDTO.Response.UserDto> getUsers(GetUsersRequest request) {
        List<Long> ids = request.ids().stream().toList();
        int from = request.from();
        int size = request.size();

        Pageable pageable = PageRequest.of(from / size, size);

        return userRepository.findUsersByIds(ids, pageable).stream()
                .map(UserMapper::toFullDto)
                .toList();
    }

    public UserDTO.Response.UserShortDto getUser(Long userId) {
        return UserMapper.toShortDto(userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователя с id: " + userId + " не найдено.")));
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
