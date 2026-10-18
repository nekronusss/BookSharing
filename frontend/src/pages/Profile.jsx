import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { FiPlus, FiSettings, FiBookOpen } from "react-icons/fi";
import { bookAPI, userAPI, exchangeAPI } from "../services/api";
import { useAuth } from "../context/AuthContext";
import BookCard from "../components/book/BookCard";
import Button from "../components/ui/Button";
export default function Profile() {
  const { user, updateUser } = useAuth(),
    [books, setBooks] = useState([]),
    [exchanges, setExchanges] = useState([]),
    [profile, setProfile] = useState({ displayName: "", email: "" }),
    [loading, setLoading] = useState(true),
    [editing, setEditing] = useState(false),
    [busy, setBusy] = useState(false),
    [status, setStatus] = useState(""),
    [error, setError] = useState("");
  const load = () => {
    setError("");
    setLoading(true);
    Promise.all([
      userAPI.getProfile(),
      bookAPI.getAll({ mine: true }),
      exchangeAPI.getAll(),
    ])
      .then(([p, b, e]) => {
        setProfile(p.data);
        setBooks(b.data.filter((x) => x.username === user.username));
        setExchanges(e.data);
      })
      .catch((e) => setError(e.formattedMessage))
      .finally(() => setLoading(false));
  };
  useEffect(load, []);
  const save = async (e) => {
    e.preventDefault();
    setBusy(true);
    setStatus("");
    setError("");
    try {
      const { data } = await userAPI.updateProfile({
        displayName: profile.displayName,
        email: profile.email,
      });
      updateUser(data);
      setProfile(data);
      setStatus("Профиль сохранён");
      setEditing(false);
    } catch (e) {
      setError(e.formattedMessage);
    } finally {
      setBusy(false);
    }
  };
  return (
    <section className="page-width section-space">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Ваша читательская история</p>
          <h1 className="page-title">
            {user.displayName || user.username}, это ваша полка
          </h1>
          <p className="text-gray-500 mt-3">
            Книги, которыми вы готовы поделиться.
          </p>
        </div>
        <Button variant="outline" onClick={() => setEditing(!editing)}>
          <FiSettings className="mr-2" />
          Настройки
        </Button>
      </div>
      {error && (
        <div className="notice error" role="alert">
          {error}
          <button className="underline ml-4" onClick={load}>
            Повторить
          </button>
        </div>
      )}
      {status && (
        <div className="notice" role="status">
          {status}
        </div>
      )}
      {editing && (
        <form className="panel mb-8" onSubmit={save}>
          <h2 className="text-2xl mb-6">Профиль читателя</h2>
          <div className="grid sm:grid-cols-2 gap-6">
            <label className="field">
              Как вас зовут
              <input
                value={profile.displayName || ""}
                maxLength={100}
                onChange={(e) =>
                  setProfile({ ...profile, displayName: e.target.value })
                }
              />
            </label>
            <label className="field">
              Электронная почта
              <input
                type="email"
                value={profile.email || ""}
                maxLength={254}
                onChange={(e) =>
                  setProfile({ ...profile, email: e.target.value })
                }
              />
            </label>
          </div>
          <p className="text-xs text-gray-500 mb-5">
            Имя пользователя: @{user.username}. Почта не отображается в
            каталоге.
          </p>
          <Button type="submit" loading={busy}>
            Сохранить профиль
          </Button>
        </form>
      )}
      <div className="stat-strip">
        <div>
          <strong>{books.length}</strong>
          <small>книг на полке</small>
        </div>
        <div>
          <strong>
            {
              exchanges.filter(
                (e) =>
                  !["COMPLETED", "REJECTED", "CANCELLED"].includes(e.status),
              ).length
            }
          </strong>
          <small>активных обменов</small>
        </div>
        <div>
          <strong>
            {exchanges.filter((e) => e.status === "COMPLETED").length}
          </strong>
          <small>завершённых обменов</small>
        </div>
      </div>
      <div className="section-heading mt-10">
        <h2>Мои книги</h2>
        <Link className="action-link" to="/books/create">
          <FiPlus />
          Добавить книгу
        </Link>
      </div>
      {loading ? (
        <div className="skeleton h-80" />
      ) : books.length ? (
        <div className="book-grid">
          {books.map((book) => (
            <BookCard key={book.id} book={book} />
          ))}
        </div>
      ) : (
        <div className="empty-panel">
          <FiBookOpen />
          <h2>У каждой полки есть начало</h2>
          <p>Добавьте первую книгу. Возможно, кто-то давно её ищет.</p>
          <Link className="text-link" to="/books/create">
            Поделиться историей →
          </Link>
        </div>
      )}
    </section>
  );
}
