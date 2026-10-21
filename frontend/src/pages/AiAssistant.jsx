import React, { useEffect, useRef, useState } from "react";
import { aiAPI } from "../services/api";
import { isDemo } from "../services/demoMode";
import Button from "../components/ui/Button";
import AiResults from "../components/ai/AiResults";

const suggestions = [
  "Подбери мне книгу",
  "Хочу что-нибудь необычное",
  "Найди похожие книги",
  "Посоветуй фэнтези",
];
export default function AiAssistant() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [usePreferences, setUsePreferences] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const generation = useRef(0);
  const end = useRef(null);
  useEffect(() => {
    end.current?.scrollIntoView?.({ behavior: "smooth", block: "nearest" });
  }, [messages, busy]);
  useEffect(
    () => () => {
      generation.current++;
    },
    [],
  );
  async function send(event) {
    event.preventDefault();
    if (!input.trim() || busy) return;
    const message = input.trim(),
      version = ++generation.current;
    const history = messages
      .filter((m) => m.role === "user")
      .slice(-4)
      .map((m) => m.message);
    setBusy(true);
    setError("");
    try {
      const { data } = await aiAPI.chat(message, history, usePreferences);
      if (version !== generation.current) return;
      setMessages((old) =>
        [
          ...old,
          { role: "user", message },
          { role: "assistant", ...data },
        ].slice(-20),
      );
      setInput("");
    } catch (e) {
      if (version === generation.current)
        setError(
          e.formattedMessage || "Не удалось получить ответ. Попробуйте снова.",
        );
    } finally {
      if (version === generation.current) setBusy(false);
    }
  }
  return (
    <section className="page-width section-space ai-chat">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Книги под ваше настроение</p>
          <h1 className="page-title">AI-помощник</h1>
        </div>
        <Button
          variant="outline"
          disabled={busy || !messages.length}
          onClick={() => {
            setMessages([]);
            setError("");
          }}
        >
          Новый диалог
        </Button>
      </div>
      <p className="ai-note">
        Помогаю выбирать из публичного каталога. Доступность передачи уточняйте
        у владельца. Сообщение и до четырёх предыдущих запросов передаются
        настроенному AI-провайдеру. Диалог хранится только на этой странице до
        её закрытия.
      </p>
      {isDemo && (
        <p className="notice">
          В деморежиме модель не подключена. Для диалога нужен реальный сервер.
        </p>
      )}
      <div className="ai-prompts">
        {suggestions.map((text) => (
          <button
            key={text}
            disabled={busy || isDemo}
            onClick={() => setInput(text)}
          >
            {text}
          </button>
        ))}
      </div>
      <div
        className="ai-messages"
        role="log"
        aria-label="Диалог с книжным помощником"
        aria-live="polite"
      >
        {!messages.length && (
          <div className="ai-welcome">
            <h2>С какой истории начнём?</h2>
            <p>Расскажите о любимой книге, теме или настроении.</p>
          </div>
        )}
        {messages.map((message, i) => (
          <article key={i} className={"ai-message " + message.role}>
            <strong>
              {message.role === "user" ? "Вы" : "Книжный помощник"}
            </strong>
            <p className="whitespace-pre-wrap">{message.message}</p>
            {message.role === "assistant" && message.items.length > 0 && (
              <AiResults result={message} />
            )}
            {message.role === "assistant" &&
              !message.items.length &&
              message.notice && <p className="ai-note">{message.notice}</p>}
          </article>
        ))}
        {busy && <p role="status">Читаю запрос и подбираю книги…</p>}
        <div ref={end} />
      </div>
      {error && (
        <p className="notice error" role="alert">
          {error}
        </p>
      )}
      <form className="ai-panel" onSubmit={send}>
        <label className="field">
          Ваш запрос
          <textarea
            rows={3}
            maxLength={1000}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Мне понравилась «1984». Есть что-то похожее?"
            required
            disabled={busy}
          />
        </label>
        <div className="ai-compose-actions">
          <label className="ai-check">
            <input
              type="checkbox"
              checked={usePreferences}
              onChange={(e) => setUsePreferences(e.target.checked)}
            />
            Учитывать мои сохранённые предпочтения
          </label>
          <Button
            type="submit"
            loading={busy}
            disabled={isDemo || !input.trim()}
          >
            Отправить
          </Button>
        </div>
      </form>
    </section>
  );
}
