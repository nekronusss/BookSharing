import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { FiBook, FiUser, FiHeart, FiLogOut, FiLogIn, FiUserPlus, FiRefreshCw, FiCamera, FiMenu, FiX } from 'react-icons/fi';
import Button from './ui/Button';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isScrolled, setIsScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => setIsScrolled(window.scrollY > 10);
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const isActive = (path) => location.pathname === path;

  const navLinks = [
    { name: 'Catalog', path: '/books', icon: <FiBook className="mr-2" />, public: true },
    { name: 'Favorites', path: '/favorites', icon: <FiHeart className="mr-2" />, public: false },
    { name: 'Exchanges', path: '/exchanges', icon: <FiRefreshCw className="mr-2" />, public: false },
    { name: 'Scan QR', path: '/scan', icon: <FiCamera className="mr-2" />, public: false },
  ];

  return (
    <nav className={`sticky top-0 z-40 transition-all duration-300 ${
      isScrolled ? 'bg-white/80 backdrop-blur-md shadow-sm py-2' : 'bg-white py-4'
    }`}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-12">
          {/* Logo */}
          <Link
            to="/"
            className="flex items-center group"
          >
            <div className="bg-primary-600 p-2 rounded-lg mr-3 group-hover:bg-primary-700 transition-colors">
              <FiBook className="text-white h-5 w-5" />
            </div>
            <span className="text-xl font-black text-gray-900 tracking-tight">
              Book<span className="text-primary-600">Sharing</span>
            </span>
          </Link>

          {/* Desktop Nav */}
          <div className="hidden md:flex items-center space-x-1">
            {navLinks.map((link) => (
              (link.public || user) && (
                <Link
                  key={link.path}
                  to={link.path}
                  className={`flex items-center px-4 py-2 rounded-lg text-sm font-bold transition-all ${
                    isActive(link.path)
                      ? 'bg-primary-50 text-primary-600'
                      : 'text-gray-500 hover:text-gray-900 hover:bg-gray-50'
                  }`}
                >
                  {link.icon}
                  {link.name}
                </Link>
              )
            ))}
          </div>

          {/* Auth Buttons */}
          <div className="hidden md:flex items-center gap-3">
            {user ? (
              <div className="flex items-center gap-2 pl-4 border-l border-gray-100">
                <Link
                  to="/profile"
                  className={`flex items-center px-3 py-2 rounded-lg text-sm font-bold transition-all ${
                    isActive('/profile') ? 'text-primary-600 bg-primary-50' : 'text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  <div className="h-8 w-8 rounded-full bg-primary-100 flex items-center justify-center text-primary-700 mr-2">
                    {user.username.charAt(0).toUpperCase()}
                  </div>
                  {user.displayName || user.username}
                </Link>
                <button
                  onClick={handleLogout}
                  className="p-2 text-gray-400 hover:text-red-600 transition-colors"
                  title="Logout"
                >
                  <FiLogOut className="h-5 w-5" />
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link to="/login">
                  <Button variant="ghost" size="sm" className="font-bold">Log In</Button>
                </Link>
                <Link to="/register">
                  <Button size="sm" className="font-bold px-6">Join Now</Button>
                </Link>
              </div>
            )}
          </div>

          {/* Mobile menu button */}
          <div className="md:hidden flex items-center">
            <button
              onClick={() => setIsMenuOpen(!isMenuOpen)}
              className="p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 focus:outline-none"
            >
              {isMenuOpen ? <FiX className="h-6 w-6" /> : <FiMenu className="h-6 w-6" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Menu */}
      {isMenuOpen && (
        <div className="md:hidden bg-white border-t border-gray-100 animate-in slide-in-from-top-4 duration-200">
          <div className="px-2 pt-2 pb-3 space-y-1">
            {navLinks.map((link) => (
              (link.public || user) && (
                <Link
                  key={link.path}
                  to={link.path}
                  onClick={() => setIsMenuOpen(false)}
                  className={`flex items-center px-3 py-3 rounded-md text-base font-bold ${
                    isActive(link.path)
                      ? 'bg-primary-50 text-primary-600'
                      : 'text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  {link.icon}
                  {link.name}
                </Link>
              )
            ))}
            {user ? (
              <>
                <Link
                  to="/profile"
                  onClick={() => setIsMenuOpen(false)}
                  className="flex items-center px-3 py-3 rounded-md text-base font-bold text-gray-700"
                >
                  <FiUser className="mr-2" /> Profile Info
                </Link>
                <button
                  onClick={handleLogout}
                  className="w-full flex items-center px-3 py-3 rounded-md text-base font-bold text-red-600 hover:bg-red-50"
                >
                  <FiLogOut className="mr-2" /> Logout
                </button>
              </>
            ) : (
              <div className="grid grid-cols-2 gap-2 p-2">
                <Link to="/login" onClick={() => setIsMenuOpen(false)}>
                  <Button variant="outline" className="w-full">Log In</Button>
                </Link>
                <Link to="/register" onClick={() => setIsMenuOpen(false)}>
                  <Button className="w-full">Sign Up</Button>
                </Link>
              </div>
            )}
          </div>
        </div>
      )}
    </nav>
  );
};

export default Navbar;





