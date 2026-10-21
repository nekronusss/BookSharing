import test from "node:test";
import assert from "node:assert/strict";
import React from "react";
import TestRenderer, { act } from "react-test-renderer";
import { MemoryRouter } from "react-router-dom";

globalThis.localStorage = {
  getItem: () => null,
  setItem() {},
  removeItem() {},
};
globalThis.window = { location: { search: "?demo=0" }, dispatchEvent() {} };
const { aiAPI } = await import("../src/services/api.js");
const { default: DescriptionAssistant } = await import(
  "../src/components/ai/DescriptionAssistant.jsx"
);
const { default: AiSearch } = await import("../src/components/ai/AiSearch.jsx");
const { default: AiAssistant } = await import("../src/pages/AiAssistant.jsx");
const text = (node) =>
  node.children.map((c) => (typeof c === "string" ? c : text(c))).join("");
const button = (tree, label) =>
  tree.root.findAllByType("button").find((n) => text(n).includes(label));
const form = {
  title: "Книга",
  author: "Автор",
  description: "Исходный текст",
  category: "Наука",
  tags: "",
};
const draft = {
  description: "Исправленный текст",
  category: "Наука",
  tags: ["наука"],
  draft: true,
};

test("description generation requires explicit apply and never overwrites on arrival", async () => {
  aiAPI.describe = async () => ({ data: draft });
  let applied = null,
    tree;
  await act(async () => {
    tree = TestRenderer.create(
      <DescriptionAssistant
        form={form}
        onApply={(d) => {
          applied = d;
        }}
      />,
    );
  });
  await act(async () => button(tree, "Помочь с описанием").props.onClick());
  assert.equal(applied, null);
  assert.match(JSON.stringify(tree.toJSON()), /Черновик AI/);
  await act(async () => button(tree, "Применить черновик").props.onClick());
  assert.deepEqual(applied, draft);
  await act(async () => tree.unmount());
});

test("changed source prevents applying a stale description", async () => {
  aiAPI.describe = async () => ({ data: draft });
  let tree;
  await act(async () => {
    tree = TestRenderer.create(
      <DescriptionAssistant form={form} onApply={() => {}} />,
    );
  });
  await act(async () => button(tree, "Помочь с описанием").props.onClick());
  await act(async () => {
    tree.update(
      <DescriptionAssistant
        form={{ ...form, description: "Новый текст" }}
        onApply={() => {}}
      />,
    );
  });
  assert.equal(button(tree, "Применить черновик").props.disabled, true);
  await act(async () => tree.unmount());
});

test("semantic search only calls on submit and displays fallback honestly", async () => {
  let count = 0,
    tree;
  aiAPI.search = async () => {
    count++;
    return {
      data: {
        items: [],
        mode: "keyword",
        notice: "Показаны текстовые совпадения",
      },
    };
  };
  await act(async () => {
    tree = TestRenderer.create(
      <MemoryRouter>
        <AiSearch category="" />
      </MemoryRouter>,
    );
  });
  await act(async () =>
    tree.root
      .findByType("textarea")
      .props.onChange({ target: { value: "Космос" } }),
  );
  assert.equal(count, 0);
  await act(async () =>
    tree.root.findByType("form").props.onSubmit({ preventDefault() {} }),
  );
  assert.equal(count, 1);
  assert.match(JSON.stringify(tree.toJSON()), /текстовые совпадения/);
  await act(async () => tree.unmount());
});

test("chat bounds session context and defaults to no personal preferences", async () => {
  const calls = [];
  aiAPI.chat = async (...args) => {
    calls.push(args);
    return {
      data: {
        message: "Уточните тему",
        items: [],
        mode: "retrieval",
        notice: "",
      },
    };
  };
  let tree;
  await act(async () => {
    tree = TestRenderer.create(
      <MemoryRouter>
        <AiAssistant />
      </MemoryRouter>,
      { createNodeMock: () => ({ scrollIntoView() {} }) },
    );
  });
  for (let i = 0; i < 6; i++) {
    await act(async () =>
      tree.root
        .findByType("textarea")
        .props.onChange({ target: { value: `Запрос ${i}` } }),
    );
    await act(async () =>
      tree.root.findByType("form").props.onSubmit({ preventDefault() {} }),
    );
  }
  assert.deepEqual(calls[5][1], [
    "Запрос 1",
    "Запрос 2",
    "Запрос 3",
    "Запрос 4",
  ]);
  assert.equal(calls[5][2], false);
  await act(async () => button(tree, "Новый диалог").props.onClick());
  assert.match(JSON.stringify(tree.toJSON()), /С какой истории начнём/);
  await act(async () => tree.unmount());
});
