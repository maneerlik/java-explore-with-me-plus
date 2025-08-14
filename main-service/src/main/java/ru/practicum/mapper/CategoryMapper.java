package ru.practicum.mapper;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.model.Category;

public final class CategoryMapper {
    /**
     * Don't let anyone instantiate this class.
     */
    private CategoryMapper() {

    }


    public static Category toCategory(NewCategoryDto newCategoryDto) {
        return new Category(null, newCategoryDto.getName());
    }

    public static Category toCategory(CategoryDto categoryDto) {
        return new Category(categoryDto.getId(), categoryDto.getName());
    }

    public static CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}