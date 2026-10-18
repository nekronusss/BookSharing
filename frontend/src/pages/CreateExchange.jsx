import React, { useEffect, useState } from "react";
import { Link, useParams, useNavigate } from "react-router-dom";
import { FiArrowLeft, FiRepeat } from "react-icons/fi";
import { bookAPI, exchangeAPI } from "../services/api";
import { useAuth } from "../context/AuthContext";
import BookCover from "../components/book/BookCover";
import Button from "../components/ui/Button";
export default function CreateExchange() {
  const { bookId } = useParams(),
    { user } = useAuth(),
    navigate = useNavigate(),
    [book, setBook] = useState(null),
    [mine, setMine] = useState([]),
    [offered, setOffered] = useState(""),
    [message, setMessage] = useState(""),
    [error, setError] = useState(""),
    [loading, setLoading] = useState(true),
    [busy, setBusy] = useState(false);
  useEffect(() => {
    if (!bookId) {
      setLoading(false);
      return;
    }
    Promise.all([bookAPI.getById(bookId), bookAPI.getAll({ mine: true })])
      .then(([b, m]) => {
        setBook(b.data);
        setMine(m.data.filter((x) => x.username === user.username));
      })
      .catch((e) => setError(e.formattedMessage))
      .finally(() => setLoading(false));
  }, [bookId]);
  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      const { data } = await exchangeAPI.create(
        Number(bookId),
        offered ? Number(offered) : null,
        message.trim(),
      );
      navigate("/exchanges/" + data.id);
    } catch (e) {
      setError(e.formattedMessage);
    } finally {
      setBusy(false);
    }
  };
  if (loading)
    return (
      <div className="page-width section-space">
        <div className="skeleton h-96" />
      </div>
    );
  if (!book || book.username === user.username)
    return (
      <div className="page-width section-space empty-panel">
        <h1 className="page-title">Найдите свою следующую книгу</h1>
        <p>{error || "Для обмена выберите книгу другого читателя."}</p>
        <Link className="action-link" to="/books">
          Открыть каталог
        </Link>
      </div>
    );
  return (
    <section className="page-width section-space">
      <Link className="text-link mb-8" to={"/books/" + bookId}>
        <FiArrowLeft />К книге
      </Link>
      <p className="eyebrow">Начните новую историю</p>
      <h1 className="page-title mb-10">Предложить обмен</h1>
      {error && (
        <div className="notice error" role="alert">
          {error}
        </div>
      )}
      <div className="detail-grid">
        <div>
          <BookCover book={book} />
          <h2 className="text-2xl mt-5">{book.title}</h2>
          <p className="text-sm text-gray-500 mt-2">
            {book.author} · @{book.username}
          </p>
        </div>
        <form className="panel" onSubmit={submit}>
          <h2 className="text-2xl mb-6">Что предложите взамен?</h2>
          <label className="field">
            Книга с вашей полки
            <select
              value={offered}
              onChange={(e) => setOffered(e.target.value)}
            >
              <option value="">Попросить передать без обмена</option>
              {mine.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.title}
                </option>
              ))}
            </select>
          </label>
          <Link to="/books/create" className="text-link mb-7">
            + Добавить книгу на свою полку
          </Link>
          <label className="field">
            Сообщение владельцу
            <textarea
              maxLength={2000}
              rows={6}
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              placeholder="Поздоровайтесь, расскажите о своей книге и предложите способ передачи. Не указывайте личные данные в публичных комментариях."
            />
          </label>
          <p className="text-xs text-gray-500 leading-6 mb-7">
            Заявка не обязывает владельца согласиться. После принятия обмена
            следите за его этапами в разделе «Мои обмены».
          </p>
          <Button type="submit" loading={busy} className="gap-3 w-full py-3">
            <FiRepeat />
            Отправить заявку
          </Button>
        </form>
      </div>
    </section>
  );
}
