<template>
  <div class="min-h-screen bg-transparent">
    <div class="container mx-auto px-4 py-8">
      <div class="max-w-4xl mx-auto">
        <!-- 页面标题 -->
        <div class="mb-8">
          <h1 class="text-3xl font-bold">
            {{ isEditMode ? "编辑文章" : "写文章" }}
          </h1>
          <p class="text-muted-foreground mt-2">
            {{ isEditMode ? "修改文章内容" : "创作你的新文章" }}
          </p>
        </div>

        <!-- 文章表单 -->
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- 标题 -->
          <div class="space-y-2">
            <label class="text-sm font-medium"
              >标题 <span class="text-destructive">*</span></label
            >
            <input
              v-model="form.title"
              type="text"
              placeholder="请输入文章标题"
              class="glass-input flex h-10 w-full rounded-md border border-input/60 bg-background/40 px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
              required
            />
          </div>

          <!-- 摘要 -->
          <div class="space-y-2">
            <label class="text-sm font-medium">摘要</label>
            <Textarea
              v-model="form.summary"
              placeholder="请输入文章摘要（可选）"
              class="min-h-25"
            />
          </div>

          <!-- 封面图片 -->
          <div class="space-y-2">
            <label class="text-sm font-medium">封面图片</label>
            <input
              v-model="form.coverUrl"
              type="url"
              placeholder="请输入封面图片 URL（可选）"
              class="glass-input flex h-10 w-full rounded-md border border-input/60 bg-background/40 px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
            />
            <div v-if="form.coverUrl" class="mt-2">
              <img
                :src="form.coverUrl"
                alt="封面预览"
                class="max-w-xs rounded-md border"
              />
            </div>
          </div>

          <!-- 分类和标签 -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <!-- 分类 -->
            <div class="space-y-2">
              <label class="text-sm font-medium">分类</label>
              <div class="space-y-2">
                <div class="relative">
                  <input
                    v-model="categorySearchKeyword"
                    type="text"
                    placeholder="搜索或输入新分类名称..."
                    class="glass-input flex h-10 w-full rounded-md border border-input/60 bg-background/40 px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                    @focus="showCategoryDropdown = true"
                  />
                  <!-- 分类下拉列表 -->
                  <div
                    v-if="showCategoryDropdown"
                    class="absolute z-10 w-full mt-1 bg-card border rounded-md shadow-lg max-h-60 overflow-auto"
                  >
                    <!-- 已选分类 -->
                    <div
                      v-if="form.categoryId"
                      class="p-2 border-b bg-muted/50"
                    >
                      <div class="flex items-center justify-between text-sm">
                        <span class="font-medium"
                          >已选：{{ getCategoryName(form.categoryId) }}</span
                        >
                        <button
                          type="button"
                          @click="clearCategory"
                          class="text-destructive hover:text-destructive/80"
                        >
                          清除
                        </button>
                      </div>
                    </div>
                    <!-- 创建新分类 -->
                    <button
                      v-if="
                        categorySearchKeyword &&
                        !filteredCategories.some(
                          (c) => c.name === categorySearchKeyword,
                        )
                      "
                      type="button"
                      @click="createNewCategory"
                      class="w-full text-left px-3 py-2 hover:bg-muted transition-colors text-sm flex items-center gap-2 text-primary"
                    >
                      <span>+ 创建分类 "{{ categorySearchKeyword }}"</span>
                    </button>
                    <!-- 分类列表 -->
                    <button
                      v-for="category in filteredCategories"
                      :key="category.id"
                      type="button"
                      @click="selectCategory(category.id)"
                      class="w-full text-left px-3 py-2 hover:bg-muted transition-colors text-sm flex items-center justify-between group"
                      :class="{
                        'bg-primary/10': form.categoryId === category.id,
                      }"
                    >
                      <span>{{ category.name }}</span>
                      <!-- 删除按钮（仅创建者可见） -->
                      <button
                        v-if="
                          category.creatorId &&
                          userStore.userInfo?.id === category.creatorId
                        "
                        type="button"
                        @click.stop="deleteCategory(category.id, category.name)"
                        class="opacity-0 group-hover:opacity-100 text-destructive hover:text-destructive/80 text-xs px-2"
                        title="删除分类"
                      >
                        删除
                      </button>
                    </button>
                    <div
                      v-if="
                        filteredCategories.length === 0 &&
                        !categorySearchKeyword
                      "
                      class="px-3 py-2 text-sm text-muted-foreground"
                    >
                      暂无分类
                    </div>
                  </div>
                </div>
                <!-- 已选分类显示 -->
                <div
                  v-if="form.categoryId && !showCategoryDropdown"
                  class="text-sm text-muted-foreground"
                >
                  已选：{{ getCategoryName(form.categoryId) }}
                </div>
              </div>
            </div>

            <!-- 标签 -->
            <div class="space-y-2">
              <label class="text-sm font-medium">标签</label>
              <div class="space-y-2">
                <!-- 已选标签 -->
                <div
                  class="flex flex-wrap gap-2 p-2 border rounded-md min-h-10"
                >
                  <Badge
                    v-for="tagId in form.tagIds"
                    :key="tagId"
                    variant="secondary"
                    class="cursor-pointer"
                    @click="removeTag(tagId)"
                  >
                    {{ getTagName(tagId) }}
                    <span class="ml-1">&times;</span>
                  </Badge>
                  <span
                    v-if="form.tagIds.length === 0"
                    class="text-sm text-muted-foreground"
                    >点击下方添加标签</span
                  >
                </div>
                <!-- 标签搜索 -->
                <div class="relative">
                  <input
                    v-model="tagSearchKeyword"
                    type="text"
                    placeholder="搜索或输入新标签名称..."
                    class="glass-input flex h-10 w-full rounded-md border border-input/60 bg-background/40 px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                    @focus="showTagDropdown = true"
                  />
                  <!-- 标签下拉列表 -->
                  <div
                    v-if="showTagDropdown"
                    class="absolute z-10 w-full mt-1 bg-card border rounded-md shadow-lg max-h-60 overflow-auto"
                  >
                    <!-- 创建新标签 -->
                    <button
                      v-if="
                        tagSearchKeyword &&
                        !filteredTags.some((t) => t.name === tagSearchKeyword)
                      "
                      type="button"
                      @click="createNewTag"
                      class="w-full text-left px-3 py-2 hover:bg-muted transition-colors text-sm flex items-center gap-2 text-primary"
                    >
                      <span>+ 创建标签 "{{ tagSearchKeyword }}"</span>
                    </button>
                    <!-- 标签列表 -->
                    <button
                      v-for="tag in filteredTags"
                      :key="tag.id"
                      type="button"
                      @click="addTag(tag.id)"
                      class="w-full text-left px-3 py-2 hover:bg-muted transition-colors text-sm flex items-center justify-between group"
                      :class="{ 'bg-muted': form.tagIds.includes(tag.id) }"
                    >
                      <span>
                        <span>{{ tag.name }}</span>
                        <span
                          v-if="form.tagIds.includes(tag.id)"
                          class="ml-2 text-primary"
                          >✓</span
                        >
                      </span>
                      <!-- 删除按钮（仅创建者可见） -->
                      <button
                        v-if="
                          tag.creatorId &&
                          userStore.userInfo?.id === tag.creatorId
                        "
                        type="button"
                        @click.stop="deleteTag(tag.id, tag.name)"
                        class="opacity-0 group-hover:opacity-100 text-destructive hover:text-destructive/80 text-xs px-2"
                        title="删除标签"
                      >
                        删除
                      </button>
                    </button>
                    <div
                      v-if="filteredTags.length === 0 && !tagSearchKeyword"
                      class="px-3 py-2 text-sm text-muted-foreground"
                    >
                      暂无标签
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 文章设置 -->
          <div class="space-y-4">
            <label class="text-sm font-medium">文章设置</label>
            <div class="flex flex-wrap gap-4">
              <label class="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  v-model="form.allowComment"
                  :true-value="1"
                  :false-value="0"
                  class="rounded"
                />
                <span class="text-sm">允许评论</span>
              </label>
              <label class="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  v-model="form.isPinned"
                  :true-value="1"
                  :false-value="0"
                  class="rounded"
                />
                <span class="text-sm">置顶文章</span>
              </label>
            </div>
          </div>

          <!-- Markdown 编辑器 -->
          <div class="space-y-2">
            <label class="text-sm font-medium"
              >文章内容 (Markdown)
              <span class="text-destructive">*</span></label
            >
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
              <!-- 编辑区 -->
              <div class="space-y-2">
                <div class="text-xs text-muted-foreground">编辑</div>
                <Textarea
                  v-model="form.contentMd"
                  placeholder="请使用 Markdown 格式编写文章内容..."
                  class="min-h-125 font-mono"
                  @paste="handlePasteImage"
                  required
                />
              </div>
              <!-- 预览区 -->
              <div class="space-y-2">
                <div class="text-xs text-muted-foreground">预览</div>
                <div
                  class="border rounded-md p-4 min-h-125 overflow-auto bg-card"
                >
                  <MarkdownRenderer
                    v-if="form.contentMd"
                    :content="form.contentMd"
                  />
                  <div v-else class="text-muted-foreground text-sm">
                    暂无内容
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="flex gap-4 justify-end pt-6 border-t">
            <Button type="button" variant="outline" @click="router.back()">
              取消
            </Button>
            <Button
              type="button"
              variant="secondary"
              @click="handleSaveDraft"
              :disabled="loading"
            >
              保存草稿
            </Button>
            <Button type="submit" :disabled="loading">
              {{ isEditMode && form.status === 1 ? "更新文章" : "发布文章" }}
            </Button>
          </div>
        </form>
      </div>
    </div>

    <!-- 删除确认对话框 -->
    <AlertDialog :open="deleteDialog.open" @update:open="closeDeleteDialog">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>确认删除</AlertDialogTitle>
          <AlertDialogDescription>
            确定要删除{{
              deleteDialog.type === "category" ? "分类" : "标签"
            }}「{{ deleteDialog.name }}」吗？<br />
            此操作不可恢复。
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel @click="closeDeleteDialog">取消</AlertDialogCancel>
          <AlertDialogAction
            @click="confirmDelete"
            class="bg-destructive hover:bg-destructive/90"
          >
            确认删除
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from "vue";
import { useRouter, useRoute } from "vue-router";
import { toast } from "vue-sonner";
import { useUserStore } from "@/stores/user";
import { articleApi } from "@/api/article";
import { categoryApi } from "@/api/category";
import { tagApi } from "@/api/tag";
import { fileApi } from "@/api/file";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import MarkdownRenderer from "@/components/Markdown/MarkdownRenderer.vue";
import type { Category, Tag } from "@/types/api";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

/**
 * 是否为编辑模式
 */
const isEditMode = computed(() => !!route.params.id);

/**
 * 加载状态
 */
const loading = ref(false);

/**
 * 表单数据
 */
const form = ref({
  title: "",
  summary: "",
  coverUrl: "",
  categoryId: null as number | null,
  columnId: null as number | null,
  tagIds: [] as number[],
  contentMd: "",
  status: 1, // 1=发布，0=草稿
  sourceType: 1, // 1=原创
  allowComment: 1, // 1=允许
  isPinned: 0, // 0=不置顶
});

/**
 * 粘贴图片到文章内容
 */
async function handlePasteImage(event: ClipboardEvent) {
  const items = event.clipboardData?.items;
  if (!items || !items.length) return;

  const imageItem = Array.from(items).find((item) =>
    item.type.startsWith("image/"),
  );
  if (!imageItem) return;

  const file = imageItem.getAsFile();
  if (!file) return;

  event.preventDefault();

  try {
    const uploaded = await fileApi.upload(file);
    const textarea = event.target as HTMLTextAreaElement | null;
    if (!textarea) return;
    const alt = uploaded.originalFilename || file.name;
    insertMarkdownAtCursor(textarea, `![${alt}](${uploaded.url})`);
    toast.success("图片已上传");
  } catch (err) {
    console.error("图片上传失败:", err);
    toast.error("图片上传失败");
  }
}

/**
 * 在光标处插入 Markdown
 */
function insertMarkdownAtCursor(textarea: HTMLTextAreaElement, value: string) {
  const current = form.value.contentMd || "";
  const start = textarea.selectionStart ?? current.length;
  const end = textarea.selectionEnd ?? current.length;
  const next = `${current.slice(0, start)}${value}${current.slice(end)}`;
  form.value.contentMd = next;

  nextTick(() => {
    const cursor = start + value.length;
    textarea.setSelectionRange(cursor, cursor);
    textarea.focus();
  });
}

/**
 * 分类列表
 */
const categories = ref<Category[]>([]);

/**
 * 标签列表
 */
const tags = ref<Tag[]>([]);

/**
 * 分类搜索关键词
 */
const categorySearchKeyword = ref("");

/**
 * 标签搜索关键词
 */
const tagSearchKeyword = ref("");

/**
 * 显示分类下拉框
 */
const showCategoryDropdown = ref(false);

/**
 * 显示标签下拉框
 */
const showTagDropdown = ref(false);

/**
 * 删除确认对话框状态
 */
const deleteDialog = ref({
  open: false,
  type: "" as "category" | "tag",
  id: 0,
  name: "",
});

/**
 * 过滤后的分类列表
 */
const filteredCategories = computed(() => {
  if (!categorySearchKeyword.value.trim()) {
    return categories.value;
  }
  const keyword = categorySearchKeyword.value.toLowerCase();
  return categories.value.filter((c) => c.name.toLowerCase().includes(keyword));
});

/**
 * 过滤后的标签列表（不包含已选）
 */
const filteredTags = computed(() => {
  let result = tags.value.filter((tag) => !form.value.tagIds.includes(tag.id));
  if (tagSearchKeyword.value.trim()) {
    const keyword = tagSearchKeyword.value.toLowerCase();
    result = result.filter((t) => t.name.toLowerCase().includes(keyword));
  }
  return result;
});

/**
 * 获取分类名称
 */
function getCategoryName(categoryId: number): string {
  const category = categories.value.find((c) => c.id === categoryId);
  return category ? category.name : "";
}

/**
 * 选择分类
 */
function selectCategory(categoryId: number) {
  form.value.categoryId = categoryId;
  showCategoryDropdown.value = false;
  categorySearchKeyword.value = getCategoryName(categoryId);
}

/**
 * 清除分类
 */
function clearCategory() {
  form.value.categoryId = null;
  categorySearchKeyword.value = "";
  showCategoryDropdown.value = false;
}

/**
 * 创建新分类
 */
async function createNewCategory() {
  const name = categorySearchKeyword.value.trim();
  if (!name) return;

  try {
    const slug = name.toLowerCase().replace(/\s+/g, "-");
    const categoryId = await categoryApi.create({ name, slug });

    // 添加到列表（包含 creatorId）
    const currentUserId = userStore.userInfo?.id;
    categories.value.push({
      id: categoryId,
      name,
      slug,
      articleCount: 0,
      creatorId: currentUserId,
    });

    // 选中新创建的分类
    selectCategory(categoryId);
    toast.success("分类创建成功", {
      description: `已创建分类「${name}」`,
    });
  } catch (error: any) {
    console.error("创建分类失败:", error);
    toast.error("创建分类失败", {
      description: error.response?.data?.message || "请重试",
    });
  }
}

/**
 * 获取标签名称
 */
function getTagName(tagId: number): string {
  const tag = tags.value.find((t) => t.id === tagId);
  return tag ? tag.name : "";
}

/**
 * 添加标签
 */
function addTag(tagId: number) {
  if (!form.value.tagIds.includes(tagId)) {
    form.value.tagIds.push(tagId);
  }
  tagSearchKeyword.value = "";
  showTagDropdown.value = false;
}

/**
 * 移除标签
 */
function removeTag(tagId: number) {
  form.value.tagIds = form.value.tagIds.filter((id) => id !== tagId);
}

/**
 * 创建新标签
 */
async function createNewTag() {
  const name = tagSearchKeyword.value.trim();
  if (!name) return;

  try {
    const slug = name.toLowerCase().replace(/\s+/g, "-");
    const tagId = await tagApi.create({ name, slug });

    // 添加到列表（包含 creatorId）
    const currentUserId = userStore.userInfo?.id;
    tags.value.push({
      id: tagId,
      name,
      slug,
      articleCount: 0,
      creatorId: currentUserId,
    });

    // 选中新创建的标签
    addTag(tagId);
    toast.success("标签创建成功", {
      description: `已创建标签「${name}」`,
    });
  } catch (error: any) {
    console.error("创建标签失败:", error);
    toast.error("创建标签失败", {
      description: error.response?.data?.message || "请重试",
    });
  }
}

/**
 * 打开删除确认对话框
 */
function openDeleteDialog(type: "category" | "tag", id: number, name: string) {
  deleteDialog.value = {
    open: true,
    type,
    id,
    name,
  };
}

/**
 * 关闭删除确认对话框
 */
function closeDeleteDialog() {
  deleteDialog.value.open = false;
}

/**
 * 确认删除
 */
async function confirmDelete() {
  if (deleteDialog.value.type === "category") {
    await deleteCategoryConfirmed(
      deleteDialog.value.id,
      deleteDialog.value.name,
    );
  } else {
    await deleteTagConfirmed(deleteDialog.value.id, deleteDialog.value.name);
  }
  closeDeleteDialog();
}

/**
 * 删除分类（打开确认对话框）
 */
function deleteCategory(categoryId: number, categoryName: string) {
  openDeleteDialog("category", categoryId, categoryName);
}

/**
 * 删除分类（已确认）
 */
async function deleteCategoryConfirmed(
  categoryId: number,
  categoryName: string,
) {
  try {
    await categoryApi.delete(categoryId);

    // 从列表中移除
    categories.value = categories.value.filter((c) => c.id !== categoryId);

    // 如果当前选中的分类被删除，清空选择
    if (form.value.categoryId === categoryId) {
      clearCategory();
    }

    toast.success("分类删除成功", {
      description: `已删除分类「${categoryName}」`,
    });
  } catch (error: any) {
    console.error("删除分类失败:", error);
    toast.error("删除失败", {
      description:
        error.response?.data?.msg ||
        error.response?.data?.message ||
        error.message ||
        "请重试",
    });
  }
}

/**
 * 删除标签（打开确认对话框）
 */
function deleteTag(tagId: number, tagName: string) {
  openDeleteDialog("tag", tagId, tagName);
}

/**
 * 删除标签（已确认）
 */
async function deleteTagConfirmed(tagId: number, tagName: string) {
  try {
    await tagApi.delete(tagId);

    // 从列表中移除
    tags.value = tags.value.filter((t) => t.id !== tagId);

    // 如果当前选中的标签被删除，从选中列表移除
    removeTag(tagId);

    toast.success("标签删除成功", {
      description: `已删除标签「${tagName}」`,
    });
  } catch (error: any) {
    console.error("删除标签失败:", error);
    toast.error("删除失败", {
      description:
        error.response?.data?.msg ||
        error.response?.data?.message ||
        error.message ||
        "请重试",
    });
  }
}

/**
 * 加载分类和标签
 */
async function loadCategoriesAndTags() {
  try {
    const [categoryList, tagList] = await Promise.all([
      categoryApi.getAll(),
      tagApi.getAll(),
    ]);
    categories.value = categoryList;
    tags.value = tagList;
  } catch (error) {
    console.error("加载分类和标签失败:", error);
  }
}

/**
 * 加载文章数据（编辑模式）
 */
async function loadArticle() {
  if (!isEditMode.value) return;

  try {
    loading.value = true;
    const id = Number(route.params.id);
    const article = await articleApi.getById(id);

    form.value.title = article.title;
    form.value.summary = article.summary || "";
    form.value.coverUrl = article.coverUrl || "";
    form.value.categoryId = article.category?.id || null;
    form.value.tagIds = article.tags?.map((t) => t.id) || [];
    form.value.contentMd = article.contentMd || "";
    form.value.status = article.status;
    form.value.sourceType = article.sourceType;
    form.value.allowComment = article.allowComment || 1;
    form.value.isPinned = article.isPinned || 0;

    // 设置分类搜索框的值
    if (form.value.categoryId) {
      categorySearchKeyword.value = getCategoryName(form.value.categoryId);
    }
  } catch (error) {
    console.error("加载文章失败:", error);
    toast.error("加载文章失败", {
      description: "请重试",
    });
    router.back();
  } finally {
    loading.value = false;
  }
}

/**
 * 提交表单（发布文章）
 */
async function handleSubmit() {
  if (!form.value.title.trim()) {
    toast.error("请输入文章标题");
    return;
  }
  if (!form.value.contentMd.trim()) {
    toast.error("请输入文章内容");
    return;
  }

  try {
    loading.value = true;
    form.value.status = 1; // 发布状态

    if (isEditMode.value) {
      const id = Number(route.params.id);
      // 处理 null 值转换为 undefined
      const updateData = {
        ...form.value,
        categoryId: form.value.categoryId ?? undefined,
        columnId: form.value.columnId ?? undefined,
      };
      await articleApi.update(id, updateData);
      toast.success("文章更新成功");
    } else {
      // 处理 null 值转换为 undefined
      const createData = {
        ...form.value,
        categoryId: form.value.categoryId ?? undefined,
        columnId: form.value.columnId ?? undefined,
      };
      const articleId = await articleApi.create(createData);
      // 获取创建的文章详情，使用 slug 跳转
      const article = await articleApi.getById(articleId);
      toast.success("文章发布成功");
      await router.push(`/articles/${article.slug}`);
      return;
    }
    router.back();
  } catch (error: any) {
    console.error("提交文章失败:", error);
    toast.error("提交失败", {
      description: error.response?.data?.message || "请重试",
    });
  } finally {
    loading.value = false;
  }
}

/**
 * 保存草稿
 */
async function handleSaveDraft() {
  if (!form.value.title.trim()) {
    toast.error("请输入文章标题");
    return;
  }

  try {
    loading.value = true;
    form.value.status = 0; // 草稿状态

    if (isEditMode.value) {
      const id = Number(route.params.id);
      // 处理 null 值转换为 undefined
      const updateData = {
        ...form.value,
        categoryId: form.value.categoryId ?? undefined,
        columnId: form.value.columnId ?? undefined,
      };
      await articleApi.update(id, updateData);
      toast.success("草稿保存成功");
    } else {
      // 处理 null 值转换为 undefined
      const createData = {
        ...form.value,
        categoryId: form.value.categoryId ?? undefined,
        columnId: form.value.columnId ?? undefined,
      };
      await articleApi.create(createData);
      toast.success("草稿保存成功");
    }
    router.back();
  } catch (error: any) {
    console.error("保存草稿失败:", error);
    toast.error("保存失败", {
      description: error.response?.data?.message || "请重试",
    });
  } finally {
    loading.value = false;
  }
}

/**
 * 初始化
 */
onMounted(async () => {
  await loadCategoriesAndTags();
  if (isEditMode.value) {
    await loadArticle();
  }

  // 点击外部关闭下拉框
  document.addEventListener("click", (e) => {
    const target = e.target as HTMLElement;
    if (!target.closest(".relative")) {
      showCategoryDropdown.value = false;
      showTagDropdown.value = false;
    }
  });
});
</script>

<style scoped>
/* 自定义样式 */
</style>
