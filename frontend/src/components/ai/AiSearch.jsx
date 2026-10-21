import React, { useEffect, useRef, useState } from "react";
import { aiAPI } from "../../services/api";
import { isDemo } from "../../services/demoMode";
import Button from "../ui/Button";
import AiResults from "./AiResults";

export default function AiSearch({ category }) {
  const [query, setQuery] = useState("");
  const [result, setResult] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const request = useRef(0);
  useEffect(() => {
    request.current++;
    setResult(null);
    setBusy(false);
    return () => {
      request.current++;
    };
  }, [category]);
  async function submit(event) {
    event.preventDefault();
    const version = ++request.current;
    setBusy(true);
    setError("");
    setResult(null);
    try {
      const { data } = await aiAPI.search(query.trim(), category);
      if (request.current === version) setResult(data);
    } catch (e) {
      if (request.current === version)
        setError(e.formattedMessage || "Не удалось выполнить поиск");
    } finally {
      if (request.current === version) setBusy(false);
    }
  }
  return (
    <div>
      <form className="ai-panel" onSubmit={submit}>
        <label className="field">
          Опишите, какую книгу хотите найти
          <textarea
            rows={3}
            maxLength={1000}
            value={query}
            required
            placeholder="Хочу что-то философское о поиске смысла жизни…"
            onChange={(e) => setQuery(e.target.value)}
          />
        </label>
        <p className="ai-note">
          Текст запроса обрабатывает настроенный AI-провайдер. Результаты —
          только из публичного каталога.
        </p>
        {isDemo && (
          <p className="ai-note">
            В деморежиме модель не подключена. Откройте реальный каталог, чтобы
            пользоваться AI.
          </p>
        )}
        <Button type="submit" loading={busy} disabled={isDemo || !query.trim()}>
          Найти по смыслу
        </Button>
      </form>
      {error && (
        <p role="alert" className="notice error">
          {error}
        </p>
      )}
      {busy && (
        <p role="status" className="ai-note">
          Ищем подходящие истории…
        </p>
      )}
      {result && <AiResults result={result} />}
    </div>
  );
}
