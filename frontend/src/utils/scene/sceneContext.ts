import { SCENE_PLAZA_DATA } from "@/data/defaults/scenePlaza";
import type { SceneCategory, SceneSubScene } from "@/types/scene/plaza";
import type { CoachHallSceneOption } from "@/types/coach/hall";

export function getAllSceneOptions(): CoachHallSceneOption[] {
  const options: CoachHallSceneOption[] = [];
  for (const cat of SCENE_PLAZA_DATA.categories) {
    for (const sub of cat.scenes) {
      options.push({
        id: sub.id,
        label: sub.title,
        categoryId: cat.id,
        categoryLabel: cat.label,
      });
    }
  }
  return options;
}

export function resolveSceneById(sceneId: string): {
  category: SceneCategory;
  sub: SceneSubScene;
} | null {
  for (const cat of SCENE_PLAZA_DATA.categories) {
    const sub = cat.scenes.find((s) => s.id === sceneId);
    if (sub) return { category: cat, sub };
  }
  return null;
}
