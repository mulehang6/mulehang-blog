package com.mulehang.blog.controller.api.v1;

import com.mulehang.blog.dto.ColumnDTO;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.service.ColumnService;
import com.mulehang.blog.vo.ColumnVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专栏 REST API。
 */
@RestController
@RequestMapping("/api/v1/columns")
@Tag(name = "专栏管理", description = "专栏相关接口")
public class ColumnController {

    private final ColumnService columnService;

    public ColumnController(ColumnService columnService) {
        this.columnService = columnService;
    }

    @PostMapping
    @Operation(summary = "创建专栏")
    public Result<Long> create(@RequestBody ColumnDTO dto) {
        return Result.ok(columnService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新专栏")
    public Result<Void> update(@PathVariable Long id, @RequestBody ColumnDTO dto) {
        columnService.update(id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除专栏")
    public Result<Void> delete(@PathVariable Long id) {
        columnService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取专栏详情")
    public Result<ColumnVO> getById(@PathVariable Long id) {
        return Result.ok(columnService.getById(id));
    }

    @GetMapping
    @Operation(summary = "专栏列表")
    public Result<List<ColumnVO>> listAll() {
        return Result.ok(columnService.listAll());
    }
}
