package biz.craftline.server.feature.businesstype.domain.service;

import biz.craftline.server.feature.businesstype.domain.model.Category;

import java.util.List;

public interface CategoryService {
    Category createCategory(String name, Long parentId);
    List<Category> getCategoryTree();
    List<Category> getCategoryPath(Long categoryId);
}
