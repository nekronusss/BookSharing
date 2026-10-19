import test, { beforeEach } from "node:test";
import assert from "node:assert/strict";
import { demoAdapter } from "../src/services/demo.js";

const storage = new Map();
globalThis.localStorage = {
  getItem: (key) => storage.get(key) ?? null,
  setItem: (key, value) => storage.set(key, String(value)),
  removeItem: (key) => storage.delete(key),
};
const login = (username) =>
  localStorage.setItem("demo-token", "demo:" + username);
const request = async (url, method = "get", data, params) =>
  (await demoAdapter({ url, method, data, params })).data;
beforeEach(() => storage.clear());

test("catalog combines author search, category and pagination", async () => {
  const first = await request("/books/search", "get", null, { size: 12 });
  assert.equal(first.content.length, 12);
  assert.equal(first.totalElements, 16);
  assert.equal(first.totalPages, 2);
  const filtered = await request("/books/search", "get", null, {
    search: "Брэдбери",
    category: "Фантастика",
  });
  assert.equal(filtered.totalElements, 1);
  assert.equal(filtered.content[0].title, "451 градус по Фаренгейту");
  const second = await request("/books/search", "get", null, {
    size: 12,
    page: 1,
  });
  assert.equal(second.content.length, 4);
});

test("full exchange requires both participants and transfers both books exactly once", async () => {
  login("alina");
  let exchange = await request("/exchanges/1/accept", "post");
  assert.equal(exchange.status, "WAITING_FOR_SHIPMENT");
  await request("/exchanges/1/mark-shipped", "post", {
    trackingNumber: "DEMO-42",
  });
  await assert.rejects(
    request("/exchanges/1/mark-delivered", "post"),
    /Действие недоступно/,
  );
  login("mark");
  await request("/exchanges/1/mark-delivered", "post");
  exchange = await request("/exchanges/1/confirm-receipt", "post");
  assert.equal(exchange.status, "DELIVERED");
  assert.equal(exchange.canConfirmReceipt, false);
  assert.equal((await request("/books/1")).username, "alina");
  login("alina");
  exchange = await request("/exchanges/1/confirm-receipt", "post");
  assert.equal(exchange.status, "COMPLETED");
  assert.equal((await request("/books/1")).username, "mark");
  assert.equal((await request("/books/6")).username, "alina");
  await assert.rejects(
    request("/exchanges/1/confirm-receipt", "post"),
    /Действие недоступно/,
  );
});

test("private exchange is inaccessible to a third user", async () => {
  await request("/auth/register", "post", {
    username: "visitor",
    password: "demo123",
  });
  login("visitor");
  await assert.rejects(
    request("/exchanges/1"),
    (error) => error.response.status === 403,
  );
  assert.deepEqual(await request("/exchanges"), []);
});

test("book creation, edit, visibility, favorites and deletion persist", async () => {
  login("alina");
  const form = new FormData();
  form.set(
    "book",
    JSON.stringify({
      title: "Test book",
      author: "Author",
      year: 2025,
      tags: [],
      isPublic: true,
    }),
  );
  const book = await request("/books", "post", form);
  form.set(
    "book",
    JSON.stringify({
      title: "Edited book",
      author: "Author",
      year: 2025,
      tags: [],
      isPublic: false,
    }),
  );
  await request("/books/" + book.id, "put", form);
  assert.equal((await request("/books/" + book.id)).title, "Edited book");
  login("mark");
  await assert.rejects(
    request("/books/" + book.id),
    (error) => error.response.status === 404,
  );
  login("alina");
  await request("/books/2/like", "post");
  assert.ok(!(await request("/books/favorites")).some((b) => b.id === 2));
  await request("/books/" + book.id, "delete");
  await assert.rejects(
    request("/books/" + book.id),
    (error) => error.response.status === 404,
  );
});

test("cannot accept competing exchanges for a reserved book", async () => {
  login("alina");
  const created = await request("/exchanges", "post", {
    requestedBookId: 2,
    offeredBookId: 1,
  });
  await request("/exchanges/1/accept", "post");
  login("mark");
  await assert.rejects(
    request("/exchanges/" + created.id + "/accept", "post"),
    /уже участвует/,
  );
});

test("QR resolves book and wrong credentials fail with an auth error", async () => {
  assert.equal((await request("/qrcode/scan/BOOK-DEMO-3")).title, "Дюна");
  await assert.rejects(
    request("/auth/login", "post", { username: "alina", password: "wrong" }),
    (error) =>
      error.response.status === 401 && error.config.url === "/auth/login",
  );
});
