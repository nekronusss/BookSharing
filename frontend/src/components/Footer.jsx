import React from "react";
import { Link } from "react-router-dom";
import { FiBookOpen } from "react-icons/fi";
import { enableDemo } from "../services/demoMode";
export default function Footer() {
  return (
    <footer className="site-footer">
      <div className="page-width footer-inner">
        <div>
          <Link to="/" className="brand">
            <FiBookOpen /> book<span>sharing</span>
            <i>·</i>
          </Link>
          <p>Истории продолжаются, когда ими делятся.</p>
        </div>
        <div className="footer-links">
          <Link to="/books">Каталог</Link>
          <Link to="/guide">Правила обмена</Link>
          <button onClick={enableDemo}>Демонстрация</button>
        </div>
        <small>© {new Date().getFullYear()} BookSharing</small>
      </div>
    </footer>
  );
}
