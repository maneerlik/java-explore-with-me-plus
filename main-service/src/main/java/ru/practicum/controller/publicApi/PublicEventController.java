package ru.practicum.controller.publicApi;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.FullEventDto;
import ru.practicum.enums.SortValue;
import ru.practicum.service.event.EventService;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEventsWithParamsByUser(@RequestParam(name = "text", required = false) String text,
                                                        @RequestParam(name = "categories", required = false) List<Long> categories,
                                                        @RequestParam(name = "paid", required = false) Boolean paid,
                                                        @RequestParam(name = "rangeStart", required = false) String rangeStart,
                                                        @RequestParam(name = "rangeEnd", required = false) String rangeEnd,
                                                        @RequestParam(name = "onlyAvailable", required = false) boolean onlyAvailable,
                                                        @RequestParam(name = "sort", required = false) SortValue sort,
                                                        @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                        @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return eventService.getEventsByUser(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    @GetMapping("/{id}")
    public FullEventDto getEvent(@PathVariable Long id) {
        return eventService.getEvent(id);
    }
}
