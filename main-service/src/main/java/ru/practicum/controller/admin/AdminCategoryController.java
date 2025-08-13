package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.service.category.CategoryService;

@RestController
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;


    @PostMapping("/admin/categories")
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        CategoryDto createdCategory = categoryService.createCategory(newCategoryDto);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        categoryService.deleteCategory(catId);
    }

    @PatchMapping("/admin/categories/{catId}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long catId,
            @Valid @RequestBody NewCategoryDto newCategoryDto
    ) {
        CategoryDto updatedCategory = categoryService.updateCategory(catId, newCategoryDto);
        return ResponseEntity.ok(updatedCategory);
    }
}
