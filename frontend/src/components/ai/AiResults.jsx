import React from "react";
import BookCard from "../book/BookCard";
export default function AiResults({
  result,
  empty = "Подходящих книг пока нет. Попробуйте уточнить запрос.",
}) {
  return (
    <div className="ai-results">
      {result?.notice && (
        <p className="ai-note" role="status">
          {result.notice}
        </p>
      )}
      {result?.items?.length ? (
        <div className="book-grid">
          {result.items.map(({ book, explanation }) => (
            <BookCard key={book.id} book={book} explanation={explanation} />
          ))}
        </div>
      ) : (
        <p className="ai-note">{empty}</p>
      )}
    </div>
  );
}
