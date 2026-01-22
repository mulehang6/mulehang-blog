import type { ApiResponse, UploadFile } from "@/types/api";
import instance from "./request";

/**
 * 文件上传 API
 */
export const fileApi = {
  /**
   * 上传文件
   */
  upload(file: File): Promise<UploadFile> {
    const form = new FormData();
    form.append("file", file);
    return instance
      .post<ApiResponse<UploadFile>>("/api/v1/files/upload", form, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      })
      .then((res) => res.data.data);
  },
};
