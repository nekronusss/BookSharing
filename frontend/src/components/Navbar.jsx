import React, { useState, useEffect } from "react";
import { Link, NavLink, useLocation, useNavigate } from "react-router-dom";
import { FiBookOpen, FiMenu, FiX, FiPlus, FiLogOut } from "react-icons/fi";
import { useAuth } from "../context/AuthContext";
export default function Navbar() {
  const { user, logout } = useAuth(),
    [open, setOpen] = useState(false),
    location = useLocation(),
    navigate = useNavigate();
  useEffect(() => setOpen(false), [location.pathname]);
  const links = [
    ["/books", "Каталог"],
    ...(user
      ? [
          ["/favorites", "Избранное"],
          ["/exchanges", "Обмены"],
          ["/scan", "QR-сканер"],
        ]
      : []),
  ];
  return (
    <header className="site-header">
      <nav className="page-width nav-inner" aria-label="Основная навигация">
        <Link to="/" className="brand">
          <span className="brand-mark">
            <FiBookOpen />
          </span>
          book<span>sharing</span>
          <i>·</i>
        </Link>
        <button
          className="mobile-toggle"
          aria-label={open ? "Закрыть меню" : "Открыть меню"}
          aria-expanded={open}
          aria-controls="navigation"
          onClick={() => setOpen(!open)}
        >
          {open ? <FiX /> : <FiMenu />}
        </button>
        <div
          id="navigation"
          className={"navigation " + (open ? "is-open" : "")}
        >
          <div className="nav-links">
            {links.map(([path, label]) => (
              <NavLink
                key={path}
                to={path}
                className={({ isActive }) => (isActive ? "active" : "")}
              >
                {label}
              </NavLink>
            ))}
            {!user && <a href="/#how-it-works">Как это работает</a>}
          </div>
          <div className="nav-actions">
            {user ? (
              <>
                <Link to="/profile" className="profile-link">
                  <span>{user.username?.[0]?.toUpperCase()}</span>Моя полка
                </Link>
                <button
                  aria-label="Выйти"
                  title="Выйти"
                  className="icon-button"
                  onClick={() => {
                    logout();
                    navigate("/");
                  }}
                >
                  <FiLogOut />
                </button>
              </>
            ) : (
              <Link to="/login" className="login-link">
                Войти
              </Link>
            )}
            <Link
              to={user ? "/books/create" : "/register"}
              className="action-link small"
            >
              <FiPlus />
              {user ? "Добавить книгу" : "Присоединиться"}
            </Link>
          </div>
        </div>
      </nav>
    </header>
  );
}
