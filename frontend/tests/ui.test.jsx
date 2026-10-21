import test from "node:test";
import assert from "node:assert/strict";
import React from "react";
import TestRenderer, { act } from "react-test-renderer";
import {
  MemoryRouter,
  Routes,
  Route,
  useLocation,
  useNavigate,
} from "react-router-dom";

const storage = new Map([
  ["booksharing-demo", "1"],
  ["demo-token", "demo:alina"],
]);
globalThis.localStorage = {
  getItem: (key) => storage.get(key) ?? null,
  setItem: (key, value) => storage.set(key, String(value)),
  removeItem: (key) => storage.delete(key),
};
globalThis.window = {
  location: { search: "?demo=1" },
  addEventListener() {},
  removeEventListener() {},
  dispatchEvent() {},
};
globalThis.document = {
  body: { style: {} },
  activeElement: { focus() {} },
  addEventListener() {},
  removeEventListener() {},
};
const { AuthProvider } = await import("../src/context/AuthContext.jsx");
const { default: PrivateRoute } = await import(
  "../src/components/PrivateRoute.jsx"
);
const { default: Home } = await import("../src/pages/Home.jsx");
const { default: Books } = await import("../src/pages/Books.jsx");
const { default: BookDetail } = await import("../src/pages/BookDetail.jsx");
const { default: Profile } = await import("../src/pages/Profile.jsx");
const { default: ExchangeDetail } = await import(
  "../src/pages/ExchangeDetail.jsx"
);
const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
let tree;
async function mount(path, pattern, Component, privatePage = false) {
  await act(async () => {
    tree = TestRenderer.create(
      <MemoryRouter initialEntries={[path]}>
        <AuthProvider>
          <Routes>
            <Route
              path={pattern}
              element={
                privatePage ? (
                  <PrivateRoute>
                    <Component />
                  </PrivateRoute>
                ) : (
                  <Component />
                )
              }
            />
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
      { createNodeMock: () => ({ querySelectorAll: () => [], focus() {} }) },
    );
    await wait(350);
  });
  await act(async () => {
    await wait(200);
  });
}
const content = () => JSON.stringify(tree.toJSON());
const text = (node) =>
  node.children.map((c) => (typeof c === "string" ? c : text(c))).join("");
const button = (label) =>
  tree.root.findAllByType("button").find((node) => text(node).includes(label));
test.afterEach(() => {
  if (tree) act(() => tree.unmount());
});

test("home renders the real demo catalog after loading", async () => {
  await mount("/", "/", Home);
  assert.match(content(), /Хорошие книги/);
  assert.match(content(), /Норвежский лес/);
  assert.doesNotMatch(content(), /Полка скоро откроется/);
});
test("catalog search updates results after debounce", async () => {
  await mount("/books", "/books", Books);
  const input = tree.root.findByProps({
    "aria-label": "Название книги или автор",
  });
  await act(async () => {
    input.props.onChange({ target: { value: "Дюна" } });
    await wait(400);
  });
  await act(async () => {
    await wait(200);
  });
  assert.match(content(), /Дюна/);
  assert.doesNotMatch(content(), /Норвежский лес/);
});
test("book detail toggles favorites and posts a comment", async () => {
  await mount("/books/2", "/books/:id", BookDetail);
  assert.match(content(), /Норвежский лес/);
  await act(async () => {
    button("В избранном").props.onClick();
    await wait(200);
  });
  assert.ok(button("В избранное"));
  await act(async () => {
    tree.root
      .findByProps({ "aria-label": "Ваш комментарий" })
      .props.onChange({ target: { value: "Тестовый отзыв" } });
  });
  await act(async () => {
    tree.root.findByType("form").props.onSubmit({ preventDefault() {} });
    await wait(300);
  });
  assert.match(content(), /Тестовый отзыв/);
});
test("profile settings save without missing component errors", async () => {
  await mount("/profile", "/profile", Profile, true);
  await act(async () => button("Настройки").props.onClick());
  await act(async () => {
    tree.root.findByType("form").props.onSubmit({ preventDefault() {} });
    await wait(200);
  });
  assert.match(content(), /Профиль сохранён/);
});
test("exchange acceptance reveals the shipment action", async () => {
  await mount("/exchanges/1", "/exchanges/:id", ExchangeDetail, true);
  await act(async () => button("Принять заявку").props.onClick());
  assert.equal(
    tree.root.findByProps({ role: "dialog" }).props["aria-modal"],
    "true",
  );
  await act(async () => {
    button("Подтвердить").props.onClick();
    await wait(200);
  });
  assert.match(content(), /Книга передана \/ отправлена/);
});

test("protected routes preserve the return URL and replace the login redirect", async () => {
  storage.delete("demo-token");
  storage.delete("demo-user");
  function LoginProbe() {
    const location = useLocation();
    const navigate = useNavigate();
    return (
      <div>
        <p>{location.state?.from}</p>
        <button onClick={() => navigate(-1)}>Back</button>
      </div>
    );
  }
  try {
    await act(async () => {
      tree = TestRenderer.create(
        <MemoryRouter
          initialEntries={["/", "/profile?section=books"]}
          initialIndex={1}
        >
          <AuthProvider>
            <Routes>
              <Route path="/" element={<p>Public home</p>} />
              <Route
                path="/profile"
                element={
                  <PrivateRoute>
                    <p>Private profile</p>
                  </PrivateRoute>
                }
              />
              <Route path="/login" element={<LoginProbe />} />
            </Routes>
          </AuthProvider>
        </MemoryRouter>,
      );
    });
    assert.match(content(), /\/profile\?section=books/);
    assert.doesNotMatch(content(), /Private profile/);
    await act(async () => button("Back").props.onClick());
    assert.match(content(), /Public home/);
  } finally {
    storage.set("demo-token", "demo:alina");
  }
});
