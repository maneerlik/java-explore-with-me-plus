package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        log.info("Создание пользователя: {}", newUserRequest);

        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            log.warn("Не удалось создать пользователя: email '{}' уже занят", newUserRequest.getEmail());
            throw new ConflictException("Email '" + newUserRequest.getEmail() + "' уже существует.");
        }

        User user = UserMapper.toUser(newUserRequest);
        User savedUser = userRepository.save(user);

        log.info("Пользователь с ID={} успешно создан", savedUser.getId());
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<User> users;

        if (ids == null || ids.isEmpty()) {
            log.info("Получение списка всех пользователей. Страница: {}, размер: {}", from / size, size);
            users = userRepository.findAll(pageable).getContent();
        } else {
            log.info("Получение списка пользователей по IDs: {}. Страница: {}, размер: {}", ids, from / size, size);
            users = userRepository.findAllByIdIn(ids, pageable);
        }

        log.debug("Найдено {} пользователей", users.size());
        return users.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUser(Long userId) {
        User user = getUserEntity(userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    public User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID=" + userId + " не найден."));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с ID={}", userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID=" + userId + " не найден. Удаление невозможно.");
        }

        try {
            userRepository.deleteById(userId);
            log.info("Пользователь с ID={} успешно удален", userId);
        } catch (Exception e) {
            log.error("Ошибка при удалении пользователя ID={}. Возможно, существуют связанные с ним данные.", userId, e);
            throw new ConflictException("Не удалось удалить пользователя. Возможно, с ним связаны другие записи.");
        }
    }
}