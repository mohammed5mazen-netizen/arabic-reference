"use client";

import type { ReactNode } from "react";
import { AdminDesk } from "@/components/admin/admin-desk";

export default function DeskLayout({ children }: { children: ReactNode }) {
  return <AdminDesk>{children}</AdminDesk>;
}
