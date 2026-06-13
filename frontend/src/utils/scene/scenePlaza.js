/**
 * 将嵌套大类数据转为卡片展示模型
 * @param {import('@/types/scene/plaza').SceneCategory} category
 * @param {import('@/types/scene/plaza').SceneSubScene} sub
 */
export function toSceneCard(category, sub) {
  return {
    id: sub.id,
    categoryId: category.id,
    categoryLabel: category.label,
    theme: category.theme,
    icon: category.icon,
    title: sub.title,
    description: sub.description,
    featureTags: sub.featureTags,
    durationMinutes: sub.durationMinutes,
    learners: sub.learners,
    imageUrl: sub.imageUrl,
  };
}
