package com.moneymanager.service;

import com.moneymanager.dto.CategoryDTO;
import com.moneymanager.entity.CategoryEntity;
import com.moneymanager.entity.ProfileEntity;
import com.moneymanager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;

    // ✅ SAVE CATEGORY (normalized type)
    public CategoryDTO saveCategory(CategoryDTO categoryDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();

        if (categoryRepository.existsByNameAndProfileId(categoryDTO.getName(), profile.getId())) {
            throw new RuntimeException("Category with this name already exists");
        }

        CategoryEntity newCategory = toEntity(categoryDTO, profile);

        // 🔥 FIX → normalize type before saving
        newCategory.setType(categoryDTO.getType().toLowerCase().trim());

        newCategory = categoryRepository.save(newCategory);
        return toDTO(newCategory);
    }

    // ✅ GET ALL CATEGORIES
    public List<CategoryDTO> getCategoriesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();

        return categoryRepository.findByProfileId(profile.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ✅ GET BY TYPE (CASE-INSENSITIVE FIX)
    public List<CategoryDTO> getCategoriesByTypeForCurrentUser(String type) {
        ProfileEntity profile = profileService.getCurrentProfile();

        String normalizedType = type.toLowerCase().trim();

        List<CategoryEntity> entities = categoryRepository
                .findByProfileId(profile.getId())
                .stream()
                .filter(cat -> cat.getType() != null &&
                        cat.getType().toLowerCase().trim().equals(normalizedType))
                .toList();

        return entities.stream().map(this::toDTO).toList();
    }

    // ✅ UPDATE CATEGORY
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();

        CategoryEntity existingCategory = categoryRepository
                .findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        existingCategory.setName(dto.getName());
        existingCategory.setIcon(dto.getIcon());

        return toDTO(categoryRepository.save(existingCategory));
    }

    // ✅ HELPER METHODS
    private CategoryEntity toEntity(CategoryDTO dto, ProfileEntity profile) {
        return CategoryEntity.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .profile(profile)
                .type(dto.getType() != null ? dto.getType().toLowerCase().trim() : null)
                .build();
    }

    private CategoryDTO toDTO(CategoryEntity entity) {
        return CategoryDTO.builder()
                .id(entity.getId())
                .profileId(entity.getProfile() != null ? entity.getProfile().getId() : null)
                .name(entity.getName())
                .icon(entity.getIcon())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .type(entity.getType())
                .build();
    }
}