package com.eems.service;

import com.eems.dto.CategoryReorderRequest;
import com.eems.dto.CategorySaveRequest;
import com.eems.vo.CategoryVO;

import java.util.List;

public interface CategoryService {
    List<CategoryVO> tree();
    CategoryVO create(CategorySaveRequest request);
    CategoryVO update(Long id, CategorySaveRequest request);
    CategoryVO delete(Long id);
    CategoryVO restore(Long id);
    void reorder(CategoryReorderRequest request);
}
