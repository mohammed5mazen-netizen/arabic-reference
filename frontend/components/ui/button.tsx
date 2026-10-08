import Link from "next/link";
import type { ButtonHTMLAttributes, ReactNode } from "react";

type Variant = "primary" | "secondary" | "ghost" | "destructive" | "link";
type Size = "small" | "medium" | "large";

const variants: Record<Variant, string> = {
  primary: "bg-library text-[var(--paper)]",
  secondary: "border border-line bg-raised text-ink",
  ghost: "bg-transparent text-ink",
  destructive: "bg-[var(--destructive)] text-[var(--paper)]",
  link: "bg-transparent px-0 text-library underline-offset-4 hover:underline",
};

const sizes: Record<Size, string> = {
  small: "min-h-10 px-3 text-sm",
  medium: "min-h-12 px-4",
  large: "min-h-14 px-6 text-base",
};

function classes(variant: Variant, size: Size, className?: string) {
  return `inline-flex items-center justify-center rounded-2xl font-medium disabled:cursor-not-allowed disabled:opacity-60 ${variants[variant]} ${sizes[size]} ${className ?? ""}`;
}

export function Button({
  variant = "primary",
  size = "medium",
  className,
  children,
  type = "button",
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: Variant; size?: Size }) {
  return (
    <button type={type} className={classes(variant, size, className)} {...props}>
      {children}
    </button>
  );
}

export function LinkButton({
  href,
  variant = "primary",
  size = "medium",
  className,
  children,
}: {
  href: string;
  variant?: Variant;
  size?: Size;
  className?: string;
  children: ReactNode;
}) {
  return (
    <Link href={href} className={classes(variant, size, className)}>
      {children}
    </Link>
  );
}
