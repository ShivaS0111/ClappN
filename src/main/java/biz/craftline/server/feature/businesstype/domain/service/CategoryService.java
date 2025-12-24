package biz.craftline.server.feature.businesstype.domain.service;

import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.domain.model.Category;

import java.util.List;

public interface CategoryService {
    Category createCategory(String name, Long parentId);
    List<Category> searchCategory(String name);
    List<Category> findAll();
    List<Category> getCategoryTree();
    List<Category> getCategoryPath(Long categoryId);
    List<Category>  findAllByIds(List<Long> categories);
    Category save(Category category);
    Category update(Long categoryId, Category category);
    void delete(Long categoryId);
}
