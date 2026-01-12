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

/**
 * 分类 Service。
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private final BlogCategoryMapper categoryMapper;

    /**
     * 构造函数（构造器注入）。
     *
     * <p>通过构造器注入依赖，避免字段注入带来的可测试性与可维护性问题。</p>
     */
    public CategoryServiceImpl(BlogCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    /**
     * 创建分类。
     *
     * @param dto 分类创建 DTO
     * @return 分类 ID
     * @throws IllegalArgumentException 当 dto 为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CategoryDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("参数 dto 不能为空");
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

    /**
     * 更新分类。
     *
     * @param id 分类 ID
     * @param dto 分类更新 DTO
     * @throws IllegalArgumentException 当 id 或 dto 为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CategoryDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("参数 id 不能为空");
        }
        if (dto == null) {
            throw new IllegalArgumentException("参数 dto 不能为空");
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

    /**
     * 删除分类。
     *
     * @param id 分类 ID
     * @throws IllegalArgumentException 当 id 为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("参数 id 不能为空");
        }
        categoryMapper.deleteById(id);
    }

    /**
     * 根据 ID 获取分类。
     *
     * @param id 分类 ID
     * @return 分类 VO
     * @throws IllegalArgumentException 当 id 为空时抛出
     */
    @Override
    public CategoryVO getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("参数 id 不能为空");
        }
        return toVO(categoryMapper.selectById(id));
    }

    /**
     * 获取所有分类。
     *
     * @return 分类列表
     */
    @Override
    public List<CategoryVO> listAll() {
        return categoryMapper.selectList(new LambdaQueryWrapper<BlogCategory>()
                        .orderByAsc(BlogCategory::getSort)
                        .orderByDesc(BlogCategory::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    /**
     * 将分类实体转换为分类 VO。
     *
     * @param c 分类实体
     * @return 分类 VO
     */
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
