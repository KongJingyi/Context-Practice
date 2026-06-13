/**
 * 场景广场 GET /api/scene-plaza
 */
import { request } from "@/api/request.js";

/**
 * @returns {Promise<import('@/types/scene/plaza').ScenePlazaApiData>}
 */
export async function fetchScenePlaza() {
  const data = await request({
    url: "/scene-plaza",
    method: "GET",
  });
  return /** @type {import('@/types/scene/plaza').ScenePlazaApiData} */ (data);
}
