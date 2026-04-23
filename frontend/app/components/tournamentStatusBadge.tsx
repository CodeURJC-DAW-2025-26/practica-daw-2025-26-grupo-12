import { getTournamentStatusMeta } from "~/services/tournament-service";

interface TournamentStatusBadgeProps {
  status?: string | null;
}

export default function TournamentStatusBadge({ status }: TournamentStatusBadgeProps) {
  const statusMeta = getTournamentStatusMeta(status);

  return <span className={`badge ${statusMeta.badgeClass}`}>{statusMeta.label}</span>;
}
