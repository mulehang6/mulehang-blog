package com.mulehang.blog.controller.api.v1;

import com.mulehang.blog.dto.TagDTO;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.service.TagService;
import com.mulehang.blog.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签 REST API。
 */
@RestController
@RequestMapping("/api/v1/tags")
@Tag(name = "标签管理", description = "标签相关接口")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    @Operation(summary = "创建标签")
    public Result<Long> create(@RequestBody TagDTO dto) {
        return Result.ok(tagService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新标签")
    public Result<Void> update(@PathVariable Long id, @RequestBody TagDTO dto) {
        tagService.update(id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除标签")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取标签详情")
    public Result<TagVO> getById(@PathVariable Long id) {
        return Result.ok(tagService.getById(id));
    }

    @GetMapping
    @Operation(summary = "标签列表")
    public Result<List<TagVO>> listAll() {
        return Result.ok(tagService.listAll());
    }
}
