import React, { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import {
  FiSearch,
  FiPlus,
  FiArrowLeft,
  FiArrowRight,
  FiBookOpen,
  FiX,
} from "react-icons/fi";
import { bookAPI } from "../services/api";
import BookCard from "../components/book/BookCard";
import Button from "../components/ui/Button";
import { useDebounce } from "../hooks/useDebounce";
import AiSearch from "../components/ai/AiSearch";
const categories = [
  "Все книги",
  "Классика",
  "Художественная литература",
  "Фантастика",
  "Нон-фикшн",
  "Наука",
  "Детские книги",
];
export default function Books() {
  const [params, setParams] = useSearchParams();
  const aiMode = params.get("mode") === "ai";
  const [search, setSearch] = useState(params.get("search") || "");
  const term = useDebounce(search, 350),
    category = params.get("category") || "",
    page = Math.max(0, Number(params.get("page")) || 0),
    sort = params.get("sort") || "addedDate,desc";
  const [result, setResult] = useState({
      content: [],
      totalElements: 0,
      totalPages: 0,
    }),
    [loading, setLoading] = useState(true),
    [error, setError] = useState(""),
    [retry, setRetry] = useState(0);
  useEffect(() => {
    if (term === (params.get("search") || "")) return;
    setParams(
      (old) => {
        const next = new URLSearchParams(old);
        term ? next.set("search", term) : next.delete("search");
        next.delete("page");
        return next;
      },
      { replace: true },
    );
  }, [term]);
  const query = params.toString();
  const urlSearch = params.get("search") || "";
  useEffect(() => setSearch(urlSearch), [urlSearch]);
  useEffect(() => {
    if (aiMode) return;
    let active = true;
    setLoading(true);
    setError("");
    bookAPI
      .search({
        search: params.get("search") || undefined,
        category: category || undefined,
        page,
        size: 12,
        sort,
      })
      .then(({ data }) => {
        if (active) setResult(data);
      })
      .catch((err) => {
        if (active) setError(err.formattedMessage);
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [query, retry]);
  const update = (key, value) =>
    setParams((old) => {
      const next = new URLSearchParams(old);
      value ? next.set(key, value) : next.delete(key);
      if (key !== "page") next.delete("page");
      return next;
    });
  return (
    <section className="page-width section-space">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Найдётся история для каждого</p>
          <h1 className="page-title">Книжная полка</h1>
          <p className="text-gray-500 mt-3">
            Откройте книгу, которую захочется передать дальше.
          </p>
        </div>
        <Link to="/books/create" className="action-link secondary">
          <FiPlus />
          Добавить книгу
        </Link>
      </div>
      <div className="category-tabs" aria-label="Режим поиска">
        <button
          className={!aiMode ? "selected" : ""}
          aria-pressed={!aiMode}
          onClick={() => update("mode", "")}
        >
          Обычный поиск
        </button>
        <button
          className={aiMode ? "selected" : ""}
          aria-pressed={aiMode}
          onClick={() => update("mode", "ai")}
        >
          AI-поиск
        </button>
      </div>
      {!aiMode && (
        <div className="catalog-search">
          <FiSearch />
          <input
            aria-label="Название книги или автор"
            placeholder="Название книги или автор…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          {search && (
            <button aria-label="Очистить поиск" onClick={() => setSearch("")}>
              <FiX />
            </button>
          )}
          <select
            aria-label="Сортировка"
            value={sort}
            onChange={(e) => update("sort", e.target.value)}
          >
            <option value="addedDate,desc">Сначала новые</option>
            <option value="title,asc">По названию</option>
            <option value="likesCount,desc">По популярности</option>
          </select>
        </div>
      )}
      <div className="category-tabs" aria-label="Жанры">
        {categories.map((c, i) => (
          <button
            key={c}
            aria-pressed={category === (i ? c : "")}
            className={category === (i ? c : "") ? "selected" : ""}
            onClick={() => update("category", i ? c : "")}
          >
            {c}
          </button>
        ))}
      </div>
      {aiMode ? (
        <AiSearch category={category} />
      ) : (
        <>
          <div className="catalog-count" role="status">
            {loading
              ? "Ищем истории…"
              : error
                ? "Каталог недоступен"
                : "Найдено книг: " + result.totalElements}
          </div>
          {error ? (
            <div className="empty-panel" role="alert">
              <h2>Не удалось загрузить книги</h2>
              <p>{error}</p>
              <Button onClick={() => setRetry((v) => v + 1)}>
                Попробовать снова
              </Button>
            </div>
          ) : loading ? (
            <div className="book-grid">
              {Array.from({ length: 8 }, (_, i) => (
                <div key={i} className="skeleton h-96 rounded-2xl" />
              ))}
            </div>
          ) : result.content.length ? (
            <>
              <div className="book-grid">
                {result.content.map((book) => (
                  <BookCard book={book} key={book.id} />
                ))}
              </div>
              {result.totalPages > 1 && (
                <div className="pagination">
                  <Button
                    variant="outline"
                    disabled={!page}
                    onClick={() => update("page", String(page - 1))}
                  >
                    <FiArrowLeft />
                    Назад
                  </Button>
                  <span>
                    {page + 1} / {result.totalPages}
                  </span>
                  <Button
                    variant="outline"
                    disabled={page + 1 >= result.totalPages}
                    onClick={() => update("page", String(page + 1))}
                  >
                    Далее
                    <FiArrowRight />
                  </Button>
                </div>
              )}
            </>
          ) : (
            <div className="empty-panel">
              <FiBookOpen />
              <h2>Пока ни одной книги</h2>
              <p>Попробуйте другой запрос или добавьте свою книгу.</p>
              <Button
                variant="outline"
                onClick={() => {
                  setSearch("");
                  setParams({});
                }}
              >
                Сбросить фильтры
              </Button>
            </div>
          )}
        </>
      )}
    </section>
  );
}
