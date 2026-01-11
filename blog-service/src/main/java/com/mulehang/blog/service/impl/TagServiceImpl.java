package com.mulehang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mulehang.blog.dto.TagDTO;
import com.mulehang.blog.entity.BlogTag;
import com.mulehang.blog.mapper.BlogTagMapper;
import com.mulehang.blog.service.TagService;
import com.mulehang.blog.vo.TagVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    private final BlogTagMapper tagMapper;

    public TagServiceImpl(BlogTagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TagDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("dto is null");
        }
        BlogTag t = new BlogTag();
        t.setName(dto.getName());
        t.setSlug(dto.getSlug());
        t.setColor(dto.getColor());
        t.setDescription(dto.getDescription());
        tagMapper.insert(t);
        return t.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, TagDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("dto is null");
        }
        BlogTag patch = new BlogTag();
        patch.setId(id);
        if (dto.getName() != null) patch.setName(dto.getName());
        if (dto.getSlug() != null) patch.setSlug(dto.getSlug());
        if (dto.getColor() != null) patch.setColor(dto.getColor());
        if (dto.getDescription() != null) patch.setDescription(dto.getDescription());
        tagMapper.updateById(patch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        tagMapper.deleteById(id);
    }

    @Override
    public TagVO getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        return toVO(tagMapper.selectById(id));
    }

    @Override
    public List<TagVO> listAll() {
        return tagMapper.selectList(new LambdaQueryWrapper<BlogTag>()
                        .orderByAsc(BlogTag::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

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
        return vo;
    }
}
