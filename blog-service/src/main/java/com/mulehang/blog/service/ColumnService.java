package com.mulehang.blog.service;

import com.mulehang.blog.dto.ColumnDTO;
import com.mulehang.blog.vo.ColumnVO;

import java.util.List;

/**
 * 专栏 Service。
 */
public interface ColumnService {

    Long create(ColumnDTO dto);

    void update(Long id, ColumnDTO dto);

    void delete(Long id);

    ColumnVO getById(Long id);

    List<ColumnVO> listAll();
}
