package com.mulehang.blog.service;

import com.mulehang.blog.dto.CategoryDTO;
import com.mulehang.blog.vo.CategoryVO;

import java.util.List;

/**
 * 分类 Service。
 */
public interface CategoryService {

    Long create(CategoryDTO dto);

    void update(Long id, CategoryDTO dto);

    void delete(Long id);

    CategoryVO getById(Long id);

    List<CategoryVO> listAll();
}
