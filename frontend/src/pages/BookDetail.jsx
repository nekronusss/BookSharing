import React, { useEffect, useState } from "react";
import { Link, useParams, useNavigate } from "react-router-dom";
import {
  FiArrowLeft,
  FiHeart,
  FiStar,
  FiEdit2,
  FiTrash2,
  FiArrowUpRight,
} from "react-icons/fi";
import { QRCodeSVG } from "qrcode.react";
import { bookAPI, commentAPI, ratingAPI } from "../services/api";
import { useAuth } from "../context/AuthContext";
import BookCover from "../components/book/BookCover";
import Button from "../components/ui/Button";
import ConfirmModal from "../components/ui/ConfirmModal";
export default function BookDetail() {
  const { id } = useParams(),
    { user } = useAuth(),
    navigate = useNavigate();
  const [book, setBook] = useState(null),
    [comments, setComments] = useState([]),
    [text, setText] = useState(""),
    [error, setError] = useState(""),
    [loading, setLoading] = useState(true),
    [busy, setBusy] = useState(false),
    [deleting, setDeleting] = useState(false),
    [score, setScore] = useState(0);
  const load = () => {
    setLoading(true);
    setError("");
    Promise.all([bookAPI.getById(id), commentAPI.getByBook(id)])
      .then(([b, c]) => {
        setBook(b.data);
        setComments(c.data);
        setScore(b.data.userScore || 0);
      })
      .catch((e) => setError(e.formattedMessage))
      .finally(() => setLoading(false));
  };
  useEffect(load, [id]);
  const act = async (fn) => {
    setBusy(true);
    setError("");
    try {
      await fn();
    } catch (e) {
      setError(e.formattedMessage);
    } finally {
      setBusy(false);
    }
  };
  if (loading)
    return (
      <div className="page-width section-space">
        <div className="skeleton h-96 rounded-2xl" />
      </div>
    );
  if (!book)
    return (
      <div className="page-width section-space empty-panel">
        <h1 className="page-title">Книга не найдена</h1>
        <p>{error}</p>
        <Button onClick={load}>Повторить</Button>
        <Link className="text-link ml-5" to="/books">
          В каталог
        </Link>
      </div>
    );
  const owner = book.username === user?.username;
  return (
    <section className="page-width section-space">
      <Link to="/books" className="text-link mb-8">
        <FiArrowLeft />
        На книжную полку
      </Link>
      {error && (
        <div className="notice error" role="alert">
          {error}
        </div>
      )}
      <div className="detail-grid">
        <BookCover key={book.imageUrl} book={book} />
        <div>
          <p className="eyebrow">{book.category || "Книжная полка"}</p>
          <h1 className="page-title">{book.title}</h1>
          <p className="text-xl text-gray-500 mt-4">{book.author}</p>
          <div className="metadata mt-6">
            <span>{book.year} год</span>
            <span>★ {Number(book.rating || 0).toFixed(1)}</span>
            <span>{book.views || 0} просмотров</span>
          </div>
          <p className="detail-description">
            {book.description || "Владелец пока не добавил описание."}
          </p>
          <div className="flex flex-wrap gap-2">
            {book.tags?.map((t) => (
              <span className="status-pill" key={t}>
                #{t}
              </span>
            ))}
          </div>
          <div className="details-actions">
            {owner ? (
              <Link className="action-link" to={"/books/" + id + "/edit"}>
                <FiEdit2 />
                Редактировать
              </Link>
            ) : (
              <Link className="action-link" to={"/exchanges/create/" + id}>
                Предложить обмен
                <FiArrowUpRight />
              </Link>
            )}
            <Button
              variant="outline"
              disabled={busy}
              onClick={() =>
                user
                  ? act(async () => {
                      const { data } = await bookAPI.toggleLike(id);
                      setBook(data);
                    })
                  : navigate("/login", { state: { from: "/books/" + id } })
              }
            >
              <FiHeart
                className={
                  book.isLikedByUser ? "fill-current text-red-500" : ""
                }
              />
              <span className="ml-2">
                {book.isLikedByUser ? "В избранном" : "В избранное"}
              </span>
            </Button>
            {owner && (
              <Button
                aria-label="Удалить книгу"
                variant="ghost"
                onClick={() => setDeleting(true)}
              >
                <FiTrash2 />
              </Button>
            )}
          </div>
          <div className="panel flex items-center gap-4">
            <div className="h-10 w-10 rounded-full bg-primary-100 flex items-center justify-center">
              {book.username?.[0]?.toUpperCase()}
            </div>
            <div>
              <p className="text-sm font-semibold">@{book.username}</p>
              <p className="text-xs text-gray-500 mt-1">
                {owner
                  ? "Книга на вашей полке"
                  : "Владелец книги · договоритесь о передаче до обмена"}
              </p>
            </div>
          </div>
          {user && (
            <div className="mt-6">
              <p className="text-sm mb-3">
                {score ? "Ваша оценка: " + score : "Как вам эта книга?"}
              </p>
              <div className="flex gap-3">
                {[1, 2, 3, 4, 5].map((n) => (
                  <button
                    aria-label={"Оценить на " + n}
                    disabled={busy}
                    key={n}
                    onClick={() =>
                      act(async () => {
                        await ratingAPI.rate(id, n, false);
                        setScore(n);
                      })
                    }
                  >
                    <FiStar
                      size={22}
                      className={
                        n <= score
                          ? "fill-current text-yellow-600"
                          : "text-gray-300"
                      }
                    />
                  </button>
                ))}
                {score > 0 && (
                  <button
                    className="text-xs text-gray-500 underline"
                    disabled={busy}
                    onClick={() =>
                      act(async () => {
                        await ratingAPI.remove(id);
                        setScore(0);
                      })
                    }
                  >
                    Убрать оценку
                  </button>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
      <div className="grid md:grid-cols-3 gap-8 mt-12">
        <div className="panel md:col-span-2">
          <h2 className="text-2xl mb-6">
            Между читателями{" "}
            <span className="text-gray-400">({comments.length})</span>
          </h2>
          {user ? (
            <form
              className="flex gap-3 mb-6"
              onSubmit={(e) => {
                e.preventDefault();
                act(async () => {
                  await commentAPI.create(id, text.trim());
                  setText("");
                  const { data } = await commentAPI.getByBook(id);
                  setComments(data);
                });
              }}
            >
              <input
                aria-label="Ваш комментарий"
                className="border rounded-lg px-4 py-3 w-full min-w-0 text-sm"
                maxLength={2000}
                required
                value={text}
                onChange={(e) => setText(e.target.value)}
                placeholder="Поделитесь впечатлением…"
              />
              <Button type="submit" disabled={busy || !text.trim()}>
                Отправить
              </Button>
            </form>
          ) : (
            <p className="text-sm text-gray-500 mb-6">
              <Link to="/login" className="text-link">
                Войдите
              </Link>
              , чтобы обсудить книгу.
            </p>
          )}
          {comments.length ? (
            comments.map((c) => (
              <div key={c.id} className="border-t py-4">
                <div className="flex justify-between">
                  <strong className="text-xs">@{c.authorUsername}</strong>
                  {user?.username === c.authorUsername && (
                    <button
                      aria-label="Удалить комментарий"
                      disabled={busy}
                      onClick={() =>
                        act(async () => {
                          await commentAPI.delete(c.id);
                          setComments((old) =>
                            old.filter((item) => item.id !== c.id),
                          );
                        })
                      }
                    >
                      <FiTrash2 size={14} />
                    </button>
                  )}
                </div>
                <p className="text-sm text-gray-600 mt-2 whitespace-pre-wrap">
                  {c.text}
                </p>
                <small className="text-gray-400">
                  {new Date(c.createdAt).toLocaleDateString("ru-RU")}
                </small>
              </div>
            ))
          ) : (
            <p className="text-sm text-gray-500">
              Здесь пока тихо. Начните разговор о книге.
            </p>
          )}
        </div>
        <aside className="panel text-center">
          <h2 className="text-2xl mb-4">Паспорт книги</h2>
          {book.qrCode ? (
            <>
              <QRCodeSVG
                value={book.qrCode}
                size={145}
                className="mx-auto"
                title="QR-код книги"
              />
              <p className="text-xs text-gray-500 mt-4 break-all">
                {book.qrCode}
              </p>
              <p className="text-xs text-gray-500 mt-3">
                Откройте QR-сканер BookSharing, чтобы найти эту карточку.
              </p>
              <Button
                variant="outline"
                className="mt-4"
                onClick={() => window.print()}
              >
                Распечатать
              </Button>
            </>
          ) : (
            <p className="text-sm text-gray-500">
              Для этой книги QR-код ещё не создан.
            </p>
          )}
        </aside>
      </div>
      <ConfirmModal
        isOpen={deleting}
        onClose={() => setDeleting(false)}
        onConfirm={() =>
          act(async () => {
            await bookAPI.delete(id);
            navigate("/profile");
          })
        }
        loading={busy}
        title="Удалить книгу?"
        message="Карточка исчезнет с вашей полки. Это действие нельзя отменить."
      />
    </section>
  );
}
