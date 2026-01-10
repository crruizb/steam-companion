import { useAchievements } from "../hooks/useAchievements";

export default function AchievementsHeatmap() {
  const { data: achievementsHeatmap } = useAchievements();

  console.log(achievementsHeatmap);

  return <div>Achievements Heatmap Component</div>;
}
