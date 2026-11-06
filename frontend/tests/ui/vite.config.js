import { fileURLToPath } from "node:url";
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Separate QA bundle. The normal production build never imports tests/ui.
export default defineConfig({
  root: fileURLToPath(new URL("../../", import.meta.url)),
  plugins: [react()],
  build: {
    outDir: "../.taste-skill/qa-build",
    emptyOutDir: true,
    rollupOptions: { input: fileURLToPath(new URL("./appointments.html", import.meta.url)) },
  },
});
