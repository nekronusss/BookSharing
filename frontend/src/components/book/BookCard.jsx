import React from "react";
import { Link } from "react-router-dom";
import { FiArrowUpRight, FiHeart, FiStar } from "react-icons/fi";
import BookCover from "./BookCover";
export default function BookCard({ book }) {
  return (
    <Link to={"/books/" + book.id} className="book-card">
      <BookCover book={book} />
      <div className="book-card-body">
        <div className="book-category">
          {book.category || "Книжная полка"}
          <FiArrowUpRight />
        </div>
        <h3>{book.title}</h3>
        <p>{book.author}</p>
        <div className="book-card-meta">
          <span>{book.username ? "@" + book.username : "Сообщество"}</span>
          <span>
            <FiStar />
            {book.rating ? Number(book.rating).toFixed(1) : "—"}
            <FiHeart />
            {book.likesCount || 0}
          </span>
        </div>
      </div>
    </Link>
  );
}
