package com.mulehang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mulehang.blog.dto.CategoryDTO;
import com.mulehang.blog.entity.BlogCategory;
import com.mulehang.blog.mapper.BlogCategoryMapper;
import com.mulehang.blog.service.CategoryService;
import com.mulehang.blog.vo.CategoryVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final BlogCategoryMapper categoryMapper;

    public CategoryServiceImpl(BlogCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CategoryDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("dto is null");
        }
        BlogCategory c = new BlogCategory();
        c.setParentId(dto.getParentId());
        c.setName(dto.getName());
        c.setSlug(dto.getSlug());
        c.setDescription(dto.getDescription());
        c.setSort(dto.getSort());
        c.setStatus(dto.getStatus());
        categoryMapper.insert(c);
        return c.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CategoryDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("dto is null");
        }
        BlogCategory patch = new BlogCategory();
        patch.setId(id);
        if (dto.getParentId() != null) patch.setParentId(dto.getParentId());
        if (dto.getName() != null) patch.setName(dto.getName());
        if (dto.getSlug() != null) patch.setSlug(dto.getSlug());
        if (dto.getDescription() != null) patch.setDescription(dto.getDescription());
        if (dto.getSort() != null) patch.setSort(dto.getSort());
        if (dto.getStatus() != null) patch.setStatus(dto.getStatus());
        categoryMapper.updateById(patch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        categoryMapper.deleteById(id);
    }

    @Override
    public CategoryVO getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        return toVO(categoryMapper.selectById(id));
    }

    @Override
    public List<CategoryVO> listAll() {
        return categoryMapper.selectList(new LambdaQueryWrapper<BlogCategory>()
                        .orderByAsc(BlogCategory::getSort)
                        .orderByDesc(BlogCategory::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    private CategoryVO toVO(BlogCategory c) {
        if (c == null) {
            return null;
        }
        CategoryVO vo = new CategoryVO();
        vo.setId(c.getId());
        vo.setParentId(c.getParentId());
        vo.setName(c.getName());
        vo.setSlug(c.getSlug());
        vo.setDescription(c.getDescription());
        vo.setSort(c.getSort());
        vo.setStatus(c.getStatus());
        return vo;
    }
}
