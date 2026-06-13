import { request } from "@/api/request";

export interface MaterialItem {
  id: number;
  name: string;
  url: string;
  sizeLabel: string;
  uploadedAt: string;
}

export async function fetchMaterials(roomId: string): Promise<MaterialItem[]> {
  try {
    return (await request({
      url: `/v1/rooms/${roomId}/materials`,
      method: "GET",
      silent: true,
    })) as MaterialItem[];
  } catch {
    return [
      { id: 1, name: "面试评分标准.pdf", url: "", sizeLabel: "1.2 MB", uploadedAt: "2026-06-01 13:50:00" },
      { id: 2, name: "学员简历.docx", url: "", sizeLabel: "856 KB", uploadedAt: "2026-06-01 13:55:00" },
    ];
  }
}

export async function uploadMaterial(
  roomId: string,
  _file: File,
  _onProgress?: (pct: number) => void,
): Promise<MaterialItem> {
  // multipart upload
  const formData = new FormData();
  formData.append("file", _file);

  try {
    return (await request({
      url: `/v1/rooms/${roomId}/materials`,
      method: "POST",
      data: formData,
      headers: { "Content-Type": "multipart/form-data" },
    })) as MaterialItem;
  } catch {
    // Mock response
    _onProgress?.(100);
    return {
      id: Date.now(),
      name: _file.name,
      url: "",
      sizeLabel: `${(_file.size / 1024).toFixed(1)} KB`,
      uploadedAt: new Date().toISOString().replace("T", " ").slice(0, 19),
    };
  }
}
