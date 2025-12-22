import React from "react";

type SteamImageProps = {
  appId: number;
  alt?: string;
  className?: string;
  placeholderSrc?: string;
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
  placeholderSrc = "/placeholder.png",
}: SteamImageProps) {
  const [index, setIndex] = React.useState(0);

  const src =
    index < IMAGE_CANDIDATES.length
      ? `https://cdn.cloudflare.steamstatic.com/steam/apps/${appId}/${IMAGE_CANDIDATES[index]}`
      : placeholderSrc;

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
