import React from "react";

type SteamImageProps = {
  appId: number;
  alt?: string;
  className?: string;
};

const IMAGE_CANDIDATES = [
  "library_600x900.jpg",
  "header.jpg",
  "capsule_467x181.jpg",
];

export function SteamImage({
  appId,
  alt = "Steam game image",
  className,
}: SteamImageProps) {
  const [index, setIndex] = React.useState(0);

  if (index >= IMAGE_CANDIDATES.length) {
    return (
      <div
        role="img"
        aria-label={alt}
        className={`${className ?? ""} flex items-center justify-center bg-linear-to-br from-line to-card p-4 text-center text-sm font-semibold text-fg`}
      >
        {alt}
      </div>
    );
  }

  const src = `https://cdn.cloudflare.steamstatic.com/steam/apps/${appId}/${IMAGE_CANDIDATES[index]}`;

  const handleError = () => {
    setIndex((prev) => prev + 1);
  };

  return (
    <img
      src={src}
      alt={alt}
      className={className}
      onError={handleError}
      loading="lazy"
    />
  );
}
