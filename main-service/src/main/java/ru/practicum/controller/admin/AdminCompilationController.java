package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.service.compilation.CompilationService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AdminCompilationController {
    private final CompilationService compilationService;


    @PostMapping("/admin/compilations")
    public ResponseEntity<CompilationDto> createCompilation(@Valid @RequestBody NewCompilationDto newDto) {
        log.info("ADMIN: creating compilation", newDto);
        CompilationDto created = compilationService.createCompilation(newDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("ADMIN: deleting compilation", compId);
        compilationService.deleteCompilation(compId);
    }

    @PatchMapping("/admin/compilations/{compId}")
    public ResponseEntity<CompilationDto> updateCompilation(
            @PathVariable Long compId,
            @Valid @RequestBody UpdateCompilationRequest updateRequest
    ) {
        log.info("ADMIN: update compilation", compId, updateRequest);
        CompilationDto updated = compilationService.updateCompilation(compId, updateRequest);
        return ResponseEntity.ok(updated);
    }
}
