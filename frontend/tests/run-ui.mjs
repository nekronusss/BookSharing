import { build } from "esbuild";
import { mkdir } from "node:fs/promises";
import { spawnSync } from "node:child_process";
await mkdir(".test-build", { recursive: true });
await build({
  entryPoints: ["tests/ui.test.jsx"],
  bundle: true,
  platform: "node",
  format: "esm",
  banner: {
    js: "import {createRequire} from 'node:module'; const require = createRequire(import.meta.url);",
  },
  external: [
    "react",
    "react-dom",
    "react-router-dom",
    "react-test-renderer",
    "axios",
    "qrcode.react",
  ],
  outfile: ".test-build/ui.test.mjs",
  define: { "import.meta.env": "{}" },
  logLevel: "warning",
});
const result = spawnSync(
  process.execPath,
  ["--test", ".test-build/ui.test.mjs"],
  { stdio: "inherit" },
);
process.exitCode = result.status ?? 1;
