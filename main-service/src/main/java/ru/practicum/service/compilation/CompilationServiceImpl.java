package ru.practicum.service.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newDto) {
        Set<Long> eventIds = newDto.getEvents();
        Set<Event> events = new HashSet<>();
        if (eventIds != null && !eventIds.isEmpty()) {
            events.addAll(eventRepository.findAllById(eventIds));
            if (events.size() != eventIds.size()) {
                throw new NotFoundException("Одно или несколько событий из списка не найдены.");
            }
        }

        Compilation compilation = new Compilation(null, events, newDto.getPinned(), newDto.getTitle());
        Compilation saved = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(saved);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Подборка с ID=" + compId + " не найдена.");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с ID=" + compId + " не найдена."));

        if (updateRequest.getEvents() != null) {
            compilation.setEvents(new HashSet<>(eventRepository.findAllById(updateRequest.getEvents())));
        }
        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }
        if (updateRequest.getTitle() != null) {
            compilation.setTitle(updateRequest.getTitle());
        }

        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public List<CompilationDto> getAllCompilations(Boolean pinned, int from, int size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, page).getContent();
        } else {
            compilations = compilationRepository.findAll(page).getContent();
        }
        return compilations.stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с ID=" + compId + " не найдена."));
        return CompilationMapper.toCompilationDto(compilation);
    }
}