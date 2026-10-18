import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { FiHeart } from "react-icons/fi";
import { bookAPI } from "../services/api";
import BookCard from "../components/book/BookCard";
import Button from "../components/ui/Button";
export default function Favorites() {
  const [books, setBooks] = useState([]),
    [loading, setLoading] = useState(true),
    [error, setError] = useState("");
  const load = () => {
    setLoading(true);
    setError("");
    bookAPI
      .getFavorites()
      .then(({ data }) => setBooks(data))
      .catch((e) => setError(e.formattedMessage))
      .finally(() => setLoading(false));
  };
  useEffect(load, []);
  return (
    <section className="page-width section-space">
      <p className="eyebrow">Сохраните на потом</p>
      <h1 className="page-title mb-4">Хочу прочитать</h1>
      <p className="text-gray-500 mb-10">
        Книги, которые зацепили. Ваша личная подборка.
      </p>
      {error ? (
        <div className="empty-panel">
          <p role="alert">{error}</p>
          <Button onClick={load}>Повторить</Button>
        </div>
      ) : loading ? (
        <div className="skeleton h-80" />
      ) : books.length ? (
        <div className="book-grid">
          {books.map((book) => (
            <BookCard book={book} key={book.id} />
          ))}
        </div>
      ) : (
        <div className="empty-panel">
          <FiHeart />
          <h2>Здесь будут ваши находки</h2>
          <p>
            Нажмите «В избранное» в карточке книги, чтобы вернуться к ней позже.
          </p>
          <Link className="action-link" to="/books">
            Выбрать книгу
          </Link>
        </div>
      )}
    </section>
  );
}
