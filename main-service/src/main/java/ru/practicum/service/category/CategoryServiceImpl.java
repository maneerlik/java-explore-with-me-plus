package ru.practicum.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category category = CategoryMapper.toCategory(newCategoryDto);
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Категория с ID=" + catId + " не найдена.");
        }
        // ДОБАВИТЬ ПРОВЕРКУ ЧТО С КАТЕГОРИЕЙ НЕ СВЯЗАНО НИ ОДНОГО СОБЫТИЯ
        categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, NewCategoryDto newCategoryDto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с ID=" + catId + " не найдена."));
        category.setName(newCategoryDto.getName());
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> getAllCategories(int from, int size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        return categoryRepository.findAll(page).stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с ID=" + catId + " не найдена."));
        return CategoryMapper.toCategoryDto(category);
    }
}