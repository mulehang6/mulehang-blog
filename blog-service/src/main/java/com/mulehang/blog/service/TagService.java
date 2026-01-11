package com.mulehang.blog.service;

import com.mulehang.blog.dto.TagDTO;
import com.mulehang.blog.vo.TagVO;

import java.util.List;

/**
 * 标签 Service。
 */
public interface TagService {

    Long create(TagDTO dto);

    void update(Long id, TagDTO dto);

    void delete(Long id);

    TagVO getById(Long id);

    List<TagVO> listAll();
}
