import React from "react";
export default class ErrorBoundary extends React.Component {
  state = { failed: false };
  static getDerivedStateFromError() {
    return { failed: true };
  }
  render() {
    if (this.state.failed)
      return (
        <div className="page-width section-space empty-panel" role="alert">
          <h1 className="page-title">Не удалось открыть страницу</h1>
          <p>Обновите приложение. Ваши сохранённые книги останутся на полке.</p>
          <button
            className="action-link"
            onClick={() => window.location.reload()}
          >
            Обновить страницу
          </button>
        </div>
      );
    return this.props.children;
  }
}
