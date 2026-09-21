package biz.craftline.server.feature.businesstype.application.service;

import biz.craftline.server.feature.businesstype.domain.model.Category;
import biz.craftline.server.feature.businesstype.infra.entity.CategoryEntity;
import biz.craftline.server.feature.businesstype.infra.mapper.CategoryEntityMapper;
import biz.craftline.server.feature.businesstype.infra.repository.CategoryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryServiceImplTest {

    @Mock private CategoryJpaRepository repository;
    @Mock private CategoryEntityMapper categoryEntityMapper;

    @InjectMocks
    private CategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCategory_withoutParent() {
        CategoryEntity saved = CategoryEntity.builder().id(1L).name("Shoes").build();
        when(repository.save(any())).thenReturn(saved);
        when(categoryEntityMapper.toDomain(saved)).thenReturn(Category.builder().id(1L).name("Shoes").build());

        Category result = service.createCategory("Shoes", null);
        assertEquals("Shoes", result.getName());
        verify(repository, never()).findById(any());
    }

    @Test
    void createCategory_withParent() {
        CategoryEntity parent = CategoryEntity.builder().id(2L).name("Root").build();
        when(repository.findById(2L)).thenReturn(Optional.of(parent));
        CategoryEntity saved = CategoryEntity.builder().id(3L).name("Child").parent(parent).build();
        when(repository.save(any())).thenReturn(saved);
        when(categoryEntityMapper.toDomain(saved)).thenReturn(Category.builder().id(3L).name("Child").parentId(2L).build());

        Category result = service.createCategory("Child", 2L);
        assertEquals(3L, result.getId());
    }

    @Test
    void createCategory_parentMissing_throws() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.createCategory("X", 9L));
    }

    @Test
    void findAll_andSearch_andTree() {
        CategoryEntity entity = CategoryEntity.builder().id(1L).name("A").build();
        Category domain = Category.builder().id(1L).name("A").build();
        when(repository.findAll()).thenReturn(List.of(entity));
        when(repository.searchByKeyword("A")).thenReturn(List.of(entity));
        when(repository.findByParentIsNull()).thenReturn(List.of(entity));
        when(categoryEntityMapper.toDomain(entity)).thenReturn(domain);

        assertEquals(1, service.findAll().size());
        assertEquals(1, service.searchCategory("A").size());
        assertEquals(1, service.getCategoryTree().size());
    }

    @Test
    void getCategoryPath_walksParents() {
        CategoryEntity root = CategoryEntity.builder().id(1L).name("Root").build();
        CategoryEntity child = CategoryEntity.builder().id(2L).name("Child").parent(root).build();
        when(repository.findById(2L)).thenReturn(Optional.of(child));
        when(categoryEntityMapper.toDomain(root)).thenReturn(Category.builder().id(1L).name("Root").build());
        when(categoryEntityMapper.toDomain(child)).thenReturn(Category.builder().id(2L).name("Child").parentId(1L).build());

        List<Category> path = service.getCategoryPath(2L);
        assertEquals(2, path.size());
        assertEquals(1L, path.get(0).getId());
        assertEquals(2L, path.get(1).getId());
    }

    @Test
    void findAllByIds() {
        CategoryEntity entity = CategoryEntity.builder().id(1L).name("A").build();
        when(repository.findAllById(List.of(1L))).thenReturn(List.of(entity));
        when(categoryEntityMapper.toDomain(entity)).thenReturn(Category.builder().id(1L).build());
        assertEquals(1, service.findAllByIds(List.of(1L)).size());
    }

    @Test
    void save_andUpdate() {
        Category category = Category.builder().name("A").description("d").status(1).build();
        CategoryEntity entity = CategoryEntity.builder().id(1L).name("A").build();
        when(categoryEntityMapper.toEntity(category)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(categoryEntityMapper.toDomain(entity)).thenReturn(Category.builder().id(1L).name("A").build());

        assertEquals(1L, service.save(category).getId());

        CategoryEntity existing = CategoryEntity.builder().id(1L).name("Old").build();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        Category update = Category.builder().name("New").description("nd").status(0).parentId(null).build();
        when(repository.save(existing)).thenReturn(existing);
        when(categoryEntityMapper.toDomain(existing)).thenReturn(Category.builder().id(1L).name("New").build());

        Category updated = service.update(1L, update);
        assertEquals("New", existing.getName());
        assertEquals("New", updated.getName());
    }

    @Test
    void update_withParent() {
        CategoryEntity existing = CategoryEntity.builder().id(1L).name("Old").build();
        CategoryEntity parent = CategoryEntity.builder().id(2L).name("Parent").build();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findById(2L)).thenReturn(Optional.of(parent));
        when(repository.save(existing)).thenReturn(existing);
        when(categoryEntityMapper.toDomain(existing)).thenReturn(Category.builder().id(1L).parentId(2L).build());

        Category result = service.update(1L, Category.builder().name("X").parentId(2L).status(1).build());
        assertEquals(parent, existing.getParent());
        assertEquals(2L, result.getParentId());
    }

    @Test
    void delete_loadsEntity() {
        CategoryEntity entity = CategoryEntity.builder().id(1L).name("A").build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        assertDoesNotThrow(() -> service.delete(1L));
    }

    @Test
    void delete_throwsWhenMissing() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.delete(1L));
    }
}
