/**
 * 场景列表 GET /api/scenes?page=1&size=10&name=
 * （文档 §3.3，无 /v1 前缀）
 */
import { request } from "@/api/request.js";

const MOCK_SCENES = [
  { id: 1, name: "压力面试", description: "连环追问与高压对话", status: 1 },
  { id: 2, name: "管理汇报", description: "年终述职与晋升答辩", status: 1 },
  { id: 3, name: "公众演讲", description: "即兴演讲与路演", status: 1 },
];

/**
 * @param {{ page?: number; size?: number; name?: string }} [query]
 * @returns {Promise<import('@/types/videoConference').SceneApiRecord[]>}
 */
export async function fetchScenes(query = {}) {
  const page = query.page ?? 1;
  const size = query.size ?? 10;
  const name = query.name ?? "";
  try {
    const data = await request({
      url: `/scenes?page=${page}&size=${size}&name=${encodeURIComponent(name)}`,
      method: "GET",
    });
    if (data?.records) return data.records;
    if (Array.isArray(data)) return data;
  } catch {
    /* mock */
  }
  await new Promise((r) => setTimeout(r, 200));
  return MOCK_SCENES;
}
