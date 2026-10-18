import React, { useState } from "react";
import { API_BASE_URL } from "../../services/api";
export default function BookCover({ book, className = "" }) {
  const [failed, setFailed] = useState(false);
  const url =
    book.imageUrl &&
    (/^(https?:\/\/|data:image\/|blob:)/.test(book.imageUrl)
      ? book.imageUrl
      : API_BASE_URL + book.imageUrl);
  return (
    <div
      className={
        "book-cover cover-tone-" + ((book.id || 0) % 4) + " " + className
      }
    >
      {url && !failed ? (
        <img
          src={url}
          alt={"Обложка книги «" + book.title + "»"}
          loading="lazy"
          onError={() => setFailed(true)}
        />
      ) : (
        <div className="typographic-cover">
          <span>BOOKSHARING / БИБЛИОТЕКА</span>
          <strong>{book.title}</strong>
          <div className="cover-decoration">✳</div>
          <small>{book.author}</small>
        </div>
      )}
    </div>
  );
}
