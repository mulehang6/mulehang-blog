package com.mulehang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mulehang.blog.context.UserContext;
import com.mulehang.blog.dto.TagDTO;
import com.mulehang.blog.entity.BlogTag;
import com.mulehang.blog.mapper.BlogTagMapper;
import com.mulehang.blog.service.TagService;
import com.mulehang.blog.vo.TagVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 标签 Service。
 */
@Service
public class TagServiceImpl implements TagService {

    private final BlogTagMapper tagMapper;

    /**
     * 构造函数（构造器注入）。
     *
     * <p>通过构造器注入依赖，避免字段注入带来的可测试性与可维护性问题。</p>
     */
    public TagServiceImpl(BlogTagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    /**
     * 创建标签。
     *
     * @param dto 标签创建 DTO
     * @return 标签 ID
     * @throws IllegalArgumentException 当 dto 为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TagDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("参数 dto 不能为空");
        }
        BlogTag t = new BlogTag();
        t.setName(dto.getName());
        t.setSlug(dto.getSlug());
        t.setColor(dto.getColor());
        t.setDescription(dto.getDescription());
        
        // 设置创建者ID
        Long currentUserId = UserContext.getCurrentUserId();
        t.setCreatorId(currentUserId);
        
        tagMapper.insert(t);
        return t.getId();
    }

    /**
     * 更新标签。
     *
     * @param id 标签 ID
     * @param dto 标签更新 DTO
     * @throws IllegalArgumentException 当 id 或 dto 为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, TagDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("参数 id 不能为空");
        }
        if (dto == null) {
            throw new IllegalArgumentException("参数 dto 不能为空");
        }
        BlogTag patch = new BlogTag();
        patch.setId(id);
        if (dto.getName() != null) patch.setName(dto.getName());
        if (dto.getSlug() != null) patch.setSlug(dto.getSlug());
        if (dto.getColor() != null) patch.setColor(dto.getColor());
        if (dto.getDescription() != null) patch.setDescription(dto.getDescription());
        tagMapper.updateById(patch);
    }

    /**
     * 删除标签。
     *
     * @param id 标签 ID
     * @throws IllegalArgumentException 当 id 为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("参数 id 不能为空");
        }
        tagMapper.deleteById(id);
    }

    /**
     * 根据 ID 获取标签。
     *
     * @param id 标签 ID
     * @return 标签 VO
     * @throws IllegalArgumentException 当 id 为空时抛出
     */
    @Override
    public TagVO getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("参数 id 不能为空");
        }
        return toVO(tagMapper.selectById(id));
    }

    /**
     * 获取所有标签。
     *
     * @return 标签列表
     */
    @Override
    public List<TagVO> listAll() {
        return tagMapper.selectList(new LambdaQueryWrapper<BlogTag>()
                        .orderByAsc(BlogTag::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    /**
     * 将标签实体转换为标签 VO。
     *
     * @param t 标签实体
     * @return 标签 VO
     */
    private TagVO toVO(BlogTag t) {
        if (t == null) {
            return null;
        }
        TagVO vo = new TagVO();
        vo.setId(t.getId());
        vo.setName(t.getName());
        vo.setSlug(t.getSlug());
        vo.setColor(t.getColor());
        vo.setDescription(t.getDescription());
        vo.setCreatorId(t.getCreatorId());
        return vo;
    }
}
