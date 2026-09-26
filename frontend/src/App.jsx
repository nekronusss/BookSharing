import React, { lazy, Suspense } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import PrivateRoute from './components/PrivateRoute';

// Lazy load pages
const Home = lazy(() => import('./pages/Home'));
const Login = lazy(() => import('./pages/Login'));
const Register = lazy(() => import('./pages/Register'));
const Books = lazy(() => import('./pages/Books'));
const BookDetail = lazy(() => import('./pages/BookDetail'));
const CreateBook = lazy(() => import('./pages/CreateBook'));
const Profile = lazy(() => import('./pages/Profile'));
const Favorites = lazy(() => import('./pages/Favorites'));
const Exchanges = lazy(() => import('./pages/Exchanges'));
const ExchangeDetail = lazy(() => import('./pages/ExchangeDetail'));
const CreateExchange = lazy(() => import('./pages/CreateExchange'));
const QRScanner = lazy(() => import('./pages/QRScanner'));

const Loading = () => (
  <div className="flex items-center justify-center min-h-screen">
    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
  </div>
);

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="min-h-screen bg-gray-50">
          <Navbar />
          <Suspense fallback={<Loading />}>
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/books" element={<Books />} />
              <Route path="/books/:id" element={<BookDetail />} />
              <Route
                path="/books/create"
                element={
                  <PrivateRoute>
                    <CreateBook />
                  </PrivateRoute>
                }
              />
              <Route
                path="/favorites"
                element={
                  <PrivateRoute>
                    <Favorites />
                  </PrivateRoute>
                }
              />
              <Route
                path="/profile"
                element={
                  <PrivateRoute>
                    <Profile />
                  </PrivateRoute>
                }
              />
              <Route
                path="/exchanges"
                element={
                  <PrivateRoute>
                    <Exchanges />
                  </PrivateRoute>
                }
              />
              <Route
                path="/exchanges/:id"
                element={
                  <PrivateRoute>
                    <ExchangeDetail />
                  </PrivateRoute>
                }
              />
              <Route
                path="/exchanges/create"
                element={
                  <PrivateRoute>
                    <CreateExchange />
                  </PrivateRoute>
                }
              />
              <Route
                path="/exchanges/create/:bookId"
                element={
                  <PrivateRoute>
                    <CreateExchange />
                  </PrivateRoute>
                }
              />
              <Route
                path="/scan"
                element={
                  <PrivateRoute>
                    <QRScanner />
                  </PrivateRoute>
                }
              />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </Suspense>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;





