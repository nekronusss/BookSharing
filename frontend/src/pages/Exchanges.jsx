import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { FiRepeat, FiArrowUpRight } from "react-icons/fi";
import { exchangeAPI } from "../services/api";
import { exchangeLabels } from "../services/exchangeStatus";
import { useAuth } from "../context/AuthContext";
import Button from "../components/ui/Button";
export default function Exchanges() {
  const { user } = useAuth(),
    [items, setItems] = useState([]),
    [loading, setLoading] = useState(true),
    [error, setError] = useState(""),
    [tab, setTab] = useState("all");
  const load = () => {
    setLoading(true);
    setError("");
    exchangeAPI
      .getAll()
      .then(({ data }) => setItems(data))
      .catch((e) => setError(e.formattedMessage))
      .finally(() => setLoading(false));
  };
  useEffect(load, []);
  const filtered = items.filter(
    (e) =>
      tab === "all" ||
      (tab === "incoming"
        ? e.offererUsername === user.username
        : tab === "outgoing"
          ? e.requesterUsername === user.username
          : e.status === "COMPLETED"),
  );
  return (
    <section className="page-width section-space">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Книги в движении</p>
          <h1 className="page-title">Мои обмены</h1>
          <p className="text-gray-500 mt-3">
            От первого «хочу прочитать» до новой истории на полке.
          </p>
        </div>
        <Link to="/books" className="action-link secondary">
          Найти книгу
          <FiArrowUpRight />
        </Link>
      </div>
      <div className="category-tabs mb-8">
        {[
          ["all", "Все обмены"],
          ["incoming", "Входящие"],
          ["outgoing", "Мои заявки"],
          ["completed", "Завершённые"],
        ].map(([key, label]) => (
          <button
            key={key}
            className={tab === key ? "selected" : ""}
            aria-pressed={tab === key}
            onClick={() => setTab(key)}
          >
            {label}
          </button>
        ))}
      </div>
      {error ? (
        <div className="empty-panel">
          <p role="alert">{error}</p>
          <Button onClick={load}>Повторить</Button>
        </div>
      ) : loading ? (
        <div className="skeleton h-64" />
      ) : filtered.length ? (
        filtered.map((e) => (
          <Link to={"/exchanges/" + e.id} key={e.id} className="exchange-card">
            <div className="exchange-meta">
              <span className="status-pill">{exchangeLabels[e.status]}</span>
              <span>{new Date(e.createdAt).toLocaleDateString("ru-RU")}</span>
            </div>
            <h3>{e.requestedBookTitle}</h3>
            <p className="text-sm text-gray-500 mb-5">
              {e.offeredBookTitle
                ? "В обмен на «" + e.offeredBookTitle + "»"
                : "Передача без встречной книги"}
            </p>
            <div className="exchange-meta">
              <span>
                {e.offererUsername === user.username
                  ? "Заявка от @" + e.requesterUsername
                  : "Ваша заявка для @" + e.offererUsername}
              </span>
              <span className="text-link">
                Открыть
                <FiArrowUpRight />
              </span>
            </div>
          </Link>
        ))
      ) : (
        <div className="empty-panel">
          <FiRepeat />
          <h2>Впереди много хороших обменов</h2>
          <p>Найдите интересную книгу в каталоге и отправьте первую заявку.</p>
          <Link className="action-link" to="/books">
            Исследовать каталог
          </Link>
        </div>
      )}
    </section>
  );
}
