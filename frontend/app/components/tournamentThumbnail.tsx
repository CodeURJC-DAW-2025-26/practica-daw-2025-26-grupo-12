import { useEffect, useState } from "react";

interface TournamentThumbnailProps {
  tournamentId: number | string;
  name: string;
  imageUrl?: string | null;
  size?: number;
  className?: string;
}

export default function TournamentThumbnail({
  tournamentId,
  name,
  imageUrl,
  size = 48,
  className = "",
}: TournamentThumbnailProps) {
  const [showFallback, setShowFallback] = useState(false);
  const src = showFallback ? null : imageUrl ?? `/api/v1/images/tournaments/${tournamentId}`;
  const initial = name.trim().charAt(0).toUpperCase() || "T";
  const sizeStyle = {
    width: `${size}px`,
    height: `${size}px`,
  };

  useEffect(() => {
    setShowFallback(false);
  }, [imageUrl, tournamentId]);

  if (!src) {
    return (
      <div
        className={`rounded-4 bg-primary d-flex align-items-center justify-content-center text-white fw-bold shadow-sm ${className}`.trim()}
        style={sizeStyle}
      >
        {initial}
      </div>
    );
  }

  return (
    <img
      src={src}
      alt={name}
      className={`rounded-4 border border-primary shadow-sm object-fit-cover ${className}`.trim()}
      style={sizeStyle}
      onError={() => setShowFallback(true)}
    />
  );
}
