package ru.practicum.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        String categoryName = newCategoryDto.getName();

        if (categoryRepository.existsByName(categoryName)) {
            throw new ConflictException("Имя категории '" + categoryName + "' уже занято.");
        }

        Category category = CategoryMapper.toCategory(newCategoryDto);
        Category savedCategory = categoryRepository.save(category);

        return CategoryMapper.toCategoryDto(savedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Категория с ID=" + catId + " не найдена.");
        }
        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Нельзя удалить категорию, с которой связаны события.");
        }
        categoryRepository.deleteById(catId);
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(Long catId, NewCategoryDto categoryDto) {
        Category categoryToUpdate = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с ID=" + catId + " не найдена."));

        String newName = categoryDto.getName();

        if (newName.equals(categoryToUpdate.getName())) {
            return CategoryMapper.toCategoryDto(categoryToUpdate);
        }

        if (categoryRepository.existsByName(newName)) {
            throw new ConflictException("Имя категории '" + newName + "' уже занято.");
        }

        categoryToUpdate.setName(newName);
        Category savedCategory = categoryRepository.save(categoryToUpdate);

        return CategoryMapper.toCategoryDto(savedCategory);
    }

    @Override
    public List<CategoryDto> getAllCategories(int from, int size) {
        PageRequest page = PageRequest.of(from / size, size);
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