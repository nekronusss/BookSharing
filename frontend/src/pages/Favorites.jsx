import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { bookAPI } from '../services/api';
import { FiHeart, FiSearch } from 'react-icons/fi';
import BookCard from '../components/book/BookCard';
import Button from '../components/ui/Button';

const Favorites = () => {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadFavorites();
  }, []);

  const loadFavorites = async () => {
    try {
      const response = await bookAPI.getFavorites();
      setBooks(response.data);
    } catch (error) {
      console.error('Error loading favorites:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-extrabold text-gray-900">My Favorites</h1>
        <p className="text-gray-500 mt-1">Books you've marked as your favorite</p>
      </div>

      {books.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-2xl border-2 border-dashed border-gray-200">
          <div className="bg-red-50 h-16 w-16 rounded-full flex items-center justify-center mx-auto mb-4">
            <FiHeart className="h-8 w-8 text-red-400" />
          </div>
          <h3 className="text-lg font-bold text-gray-900 mb-2">Your collection is empty</h3>
          <p className="text-gray-500 mb-8 max-w-sm mx-auto">
            Explore our catalog and heart the books you'd like to save for later or swap.
          </p>
          <Button
            onClick={() => window.location.href = '/books'}
            variant="primary"
          >
            Explore Catalog
          </Button>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
          {books.map((book) => (
            <BookCard key={book.id} book={book} />
          ))}
        </div>
      )}
    </div>
  );
};

export default Favorites;







