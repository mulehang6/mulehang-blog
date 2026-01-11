package com.mulehang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mulehang.blog.dto.ColumnDTO;
import com.mulehang.blog.entity.BlogColumn;
import com.mulehang.blog.mapper.BlogColumnMapper;
import com.mulehang.blog.service.ColumnService;
import com.mulehang.blog.vo.ColumnVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ColumnServiceImpl implements ColumnService {

    private final BlogColumnMapper columnMapper;

    public ColumnServiceImpl(BlogColumnMapper columnMapper) {
        this.columnMapper = columnMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ColumnDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("dto is null");
        }
        BlogColumn c = new BlogColumn();
        c.setName(dto.getName());
        c.setSlug(dto.getSlug());
        c.setCoverUrl(dto.getCoverUrl());
        c.setDescription(dto.getDescription());
        c.setSort(dto.getSort());
        c.setStatus(dto.getStatus());
        columnMapper.insert(c);
        return c.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ColumnDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("dto is null");
        }
        BlogColumn patch = new BlogColumn();
        patch.setId(id);
        if (dto.getName() != null) patch.setName(dto.getName());
        if (dto.getSlug() != null) patch.setSlug(dto.getSlug());
        if (dto.getCoverUrl() != null) patch.setCoverUrl(dto.getCoverUrl());
        if (dto.getDescription() != null) patch.setDescription(dto.getDescription());
        if (dto.getSort() != null) patch.setSort(dto.getSort());
        if (dto.getStatus() != null) patch.setStatus(dto.getStatus());
        columnMapper.updateById(patch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        columnMapper.deleteById(id);
    }

    @Override
    public ColumnVO getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        return toVO(columnMapper.selectById(id));
    }

    @Override
    public List<ColumnVO> listAll() {
        return columnMapper.selectList(new LambdaQueryWrapper<BlogColumn>()
                        .orderByAsc(BlogColumn::getSort)
                        .orderByDesc(BlogColumn::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    private ColumnVO toVO(BlogColumn c) {
        if (c == null) {
            return null;
        }
        ColumnVO vo = new ColumnVO();
        vo.setId(c.getId());
        vo.setName(c.getName());
        vo.setSlug(c.getSlug());
        vo.setCoverUrl(c.getCoverUrl());
        vo.setDescription(c.getDescription());
        vo.setSort(c.getSort());
        vo.setStatus(c.getStatus());
        return vo;
    }
}
