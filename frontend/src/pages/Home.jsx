import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  FiArrowUpRight,
  FiArrowRight,
  FiBookOpen,
  FiRepeat,
  FiHeart,
  FiPlus,
} from "react-icons/fi";
import { bookAPI } from "../services/api";
import BookCard from "../components/book/BookCard";
import { useAuth } from "../context/AuthContext";
import Recommendations from "../components/ai/Recommendations";
export default function Home() {
  const { user } = useAuth();
  const [books, setBooks] = useState([]),
    [status, setStatus] = useState("loading");
  useEffect(() => {
    let active = true;
    bookAPI
      .search({ size: 4, sort: "addedDate,desc" })
      .then(({ data }) => {
        if (active) {
          setBooks(data.content);
          setStatus("ready");
        }
      })
      .catch(() => {
        if (active) setStatus("error");
      });
    return () => {
      active = false;
    };
  }, []);
  return (
    <>
      <section className="hero page-width">
        <div className="hero-copy">
          <p className="eyebrow">
            <span className="status-dot" />
            Книги объединяют
          </p>
          <h1>
            Хорошие книги
            <br />
            должны{" "}
            <em>
              жить
              <br />
              дальше.
            </em>
          </h1>
          <p className="hero-description">
            На вашей полке — чья-то следующая любимая история. Делитесь
            прочитанным, находите новое и знакомьтесь через книги.
          </p>
          <div className="flex flex-wrap gap-3 mt-8">
            <Link className="action-link" to="/books">
              Найти свою книгу <FiArrowUpRight />
            </Link>
            <Link
              className="action-link secondary"
              to={user ? "/books/create" : "/register"}
            >
              <FiPlus />
              Поделиться книгой
            </Link>
          </div>
          <div className="hero-note">
            <FiRepeat />
            Меньше покупок. Больше историй.
          </div>
        </div>
        <div
          className="book-scene"
          role="img"
          aria-label="Книги ждут нового читателя"
        >
          <div className="scene-orbit" />
          <span className="scene-caption">У каждой книги есть продолжение</span>
          <div className="scene-book book-one">
            <span>
              БИБЛИОТЕКА
              <br />
              БОЛЬШИХ ИСТОРИЙ
            </span>
            <strong>
              Маленький
              <br />
              принц
            </strong>
            <div className="cover-sun" />
            <small>Антуан де Сент-Экзюпери</small>
          </div>
          <div className="scene-book book-two">
            <span>ОТКРЫВАЙТЕ НОВОЕ</span>
            <strong>
              Навстречу
              <br />
              <i>приключениям</i>
            </strong>
            <div className="cover-arch" />
            <small>Ваша следующая глава</small>
          </div>
          <div className="scene-ticket">
            <FiHeart />
            <div>
              Прочитано с любовью<small>Передаётся дальше</small>
            </div>
          </div>
          <span className="scene-spark">✳</span>
        </div>
      </section>
      <div className="principles">
        <div className="page-width flex flex-wrap justify-between gap-5">
          <span>
            <FiBookOpen />
            Бумажные книги, настоящие истории
          </span>
          <span>
            <FiRepeat />
            Обмен без оплаты на платформе
          </span>
          <span>
            <FiHeart />
            От читателя к читателю
          </span>
        </div>
      </div>
      <section className="page-width section-space">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Новая глава начинается здесь</p>
            <h2>На книжной полке</h2>
          </div>
          <Link to="/books" className="text-link">
            Весь каталог <FiArrowRight />
          </Link>
        </div>
        {status === "loading" ? (
          <div className="book-grid" aria-label="Загрузка книг">
            {[1, 2, 3, 4].map((n) => (
              <div key={n} className="skeleton h-80 rounded-2xl" />
            ))}
          </div>
        ) : books.length ? (
          <div className="book-grid">
            {books.map((book) => (
              <BookCard key={book.id} book={book} />
            ))}
          </div>
        ) : (
          <div className="empty-panel">
            <FiBookOpen />
            <h3>
              {status === "error"
                ? "Полка скоро откроется"
                : "Первая история может быть вашей"}
            </h3>
            <p>
              {status === "error"
                ? "Не удалось связаться с каталогом. Попробуйте открыть его ещё раз."
                : "Добавьте прочитанную книгу и помогите ей найти нового читателя."}
            </p>
            <Link
              className="text-link"
              to={status === "error" ? "/books" : "/books/create"}
            >
              {status === "error" ? "Открыть каталог" : "Добавить книгу"}
              <FiArrowRight />
            </Link>
          </div>
        )}
      </section>
      {user && <Recommendations key={user.id || user.username} />}
      <section id="how-it-works" className="how-section">
        <div className="page-width section-space">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Всё начинается с одной книги</p>
              <h2>Прочитали. Поделились. Повторили.</h2>
            </div>
          </div>
          <div className="steps-grid">
            {[
              [
                "01",
                "Освободите место для нового",
                "Добавьте книгу, обложку и описание. Расскажите о состоянии экземпляра.",
              ],
              [
                "02",
                "Найдите свою следующую историю",
                "Выберите книгу и отправьте заявку. Предложите свою книгу взамен или попросите передать её без обмена.",
              ],
              [
                "03",
                "Передайте книгу дальше",
                "Согласуйте передачу, отметьте отправку и получение. После подтверждения обеих сторон книга сменит владельца.",
              ],
            ].map(([n, t, d]) => (
              <article key={n}>
                <span className="step-number">{n}</span>
                <h3>{t}</h3>
                <p>{d}</p>
              </article>
            ))}
          </div>
        </div>
      </section>
      <section className="page-width section-space">
        <div className="join-panel">
          <div>
            <p className="eyebrow">Вместе читать интереснее</p>
            <h2>
              Не оставляйте истории
              <br />
              пылиться на полке.
            </h2>
            <p>Дайте книге нового читателя. А себе — повод открыть другую.</p>
          </div>
          <Link
            to={user ? "/books/create" : "/register"}
            className="action-link light"
          >
            {user ? "Добавить книгу" : "Присоединиться"}
            <FiArrowUpRight />
          </Link>
        </div>
      </section>
    </>
  );
}
