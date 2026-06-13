import { request } from "@/api/request";

export interface MaterialItem {
  id: number;
  name: string;
  url: string;
  sizeLabel: string;
  uploadedAt: string;
}

export async function fetchMaterials(roomId: string): Promise<MaterialItem[]> {
  return (await request({
    url: `/v1/rooms/${roomId}/materials`,
    method: "GET",
    silent: true,
  })) as MaterialItem[];
}

export async function uploadMaterial(
  roomId: string,
  file: File,
  _onProgress?: (pct: number) => void,
): Promise<MaterialItem> {
  const formData = new FormData();
  formData.append("file", file);
  _onProgress?.(50);
  const result = (await request({
    url: `/v1/rooms/${roomId}/materials`,
    method: "POST",
    data: formData,
    headers: { "Content-Type": "multipart/form-data" },
  })) as MaterialItem;
  _onProgress?.(100);
  return result;
}
