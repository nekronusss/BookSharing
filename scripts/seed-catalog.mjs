import { readFile, writeFile, mkdir, open, unlink } from "node:fs/promises";
import { randomBytes } from "node:crypto";
import { fileURLToPath } from "node:url";
import path from "node:path";

const root = fileURLToPath(new URL("../", import.meta.url));
const base = new URL(process.argv[2] || "http://127.0.0.1:8080");
if (
  !["127.0.0.1", "localhost", "[::1]"].includes(base.hostname) ||
  base.protocol !== "http:" ||
  base.username ||
  base.password ||
  base.pathname !== "/" ||
  base.search ||
  base.hash
) {
  throw new Error(
    "Starter catalog import supports only a local HTTP backend origin.",
  );
}
const books = JSON.parse(
  await readFile(path.join(root, "data/starter-books.json"), "utf8"),
);
const keys = new Set();
const keyOf = (b) => `${b.title}\0${b.author}`;
for (const book of books) {
  if (
    !book.title ||
    !book.author ||
    !Number.isInteger(book.year) ||
    !book.description ||
    !book.category ||
    !Array.isArray(book.tags) ||
    keys.has(keyOf(book))
  )
    throw new Error("Invalid or duplicate starter book.");
  keys.add(keyOf(book));
}
const local = path.join(root, ".local");
await mkdir(local, { recursive: true });
const lockPath = path.join(local, "seed-catalog.lock");
const lock = await open(lockPath, "wx");
const accountPath = path.join(
  local,
  `starter-account-${base.port || "80"}.json`,
);
let token;
async function api(
  route,
  { method = "GET", body, multipart = false, allowUnauthorized = false } = {},
) {
  const headers = token ? { Authorization: `Bearer ${token}` } : {};
  if (body && !multipart) headers["Content-Type"] = "application/json";
  const response = await fetch(new URL(route, base), {
    method,
    headers,
    body: body ? (multipart ? body : JSON.stringify(body)) : undefined,
    signal: AbortSignal.timeout(30000),
  });
  if (allowUnauthorized && response.status === 401) return null;
  if (!response.ok)
    throw new Error(`${method} ${route}: HTTP ${response.status}`);
  return response.json();
}
try {
  let account;
  try {
    account = JSON.parse(await readFile(accountPath, "utf8"));
  } catch (error) {
    if (error.code !== "ENOENT") throw error;
    account = {
      baseUrl: base.origin,
      username: `starter-library-${randomBytes(4).toString("hex")}`,
      password: randomBytes(24).toString("base64url"),
    };
    await writeFile(accountPath, JSON.stringify(account, null, 2) + "\n", {
      flag: "wx",
      mode: 0o600,
    });
  }
  if (account.baseUrl !== base.origin)
    throw new Error("Saved account belongs to a different backend origin.");
  const credentials = {
    username: account.username,
    password: account.password,
  };
  let login = await api("/auth/login", {
    method: "POST",
    body: credentials,
    allowUnauthorized: true,
  });
  if (!login) {
    await api("/auth/register", { method: "POST", body: credentials });
    login = await api("/auth/login", { method: "POST", body: credentials });
  }
  token = login.token;
  if (!token) throw new Error("Backend did not return an access token.");
  const catalog = await api("/books");
  const present = new Set(catalog.map(keyOf));
  let created = 0;
  for (const book of books) {
    if (present.has(keyOf(book))) continue;
    const payload = {
      ...book,
      isPublic: true,
      description: `${book.description}\n\nКарточка стартового каталога. Наличие физического экземпляра и его состояние не подтверждены. Год относится к первой публикации произведения.`,
    };
    const form = new FormData();
    form.append("book", JSON.stringify(payload));
    await api("/books", { method: "POST", body: form, multipart: true });
    present.add(keyOf(book));
    created++;
    console.log(`Added: ${book.title}`);
  }
  const after = await api("/books");
  const actual = new Set(after.map(keyOf));
  if (books.some((book) => !actual.has(keyOf(book))))
    throw new Error("Catalog verification failed.");
  console.log(
    `Created ${created}; skipped ${books.length - created}; verified ${books.length} starter books.`,
  );
  console.log(
    "Local owner credentials: " +
      path.relative(root, accountPath) +
      " (ignored by Git).",
  );
} finally {
  await lock.close();
  await unlink(lockPath);
}
