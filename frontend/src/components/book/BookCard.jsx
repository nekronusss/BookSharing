import React from 'react';
import { Link } from 'react-router-dom';
import { FiStar, FiEye, FiHeart } from 'react-icons/fi';
import Badge from '../ui/Badge';

const BookCard = ({ book }) => {
  const getImageUrl = (imageUrl) => {
    if (!imageUrl) return 'https://via.placeholder.com/300x400?text=No+Image';
    if (imageUrl.startsWith('http')) return imageUrl;
    const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
    return `${API_BASE_URL}${imageUrl}`;
  };

  return (
    <Link
      to={`/books/${book.id}`}
      className="group bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-all duration-300 flex flex-col h-full"
    >
      <div className="relative aspect-[3/4] overflow-hidden bg-gray-100">
        <img
          src={getImageUrl(book.imageUrl)}
          alt={book.title}
          className="w-full h-full object-cover transform group-hover:scale-105 transition-transform duration-500"
        />
        {book.category && (
          <div className="absolute top-2 left-2">
            <Badge variant="primary" className="opacity-90 shadow-sm">
              {book.category}
            </Badge>
          </div>
        )}
      </div>
      
      <div className="p-4 flex-1 flex flex-col">
        <h3 className="text-base font-bold text-gray-900 mb-1 line-clamp-2 group-hover:text-primary-600 transition-colors">
          {book.title}
        </h3>
        <p className="text-sm text-gray-500 mb-3">by {book.author}</p>
        
        <div className="mt-auto flex items-center justify-between pt-3 border-t border-gray-50">
          <div className="flex items-center space-x-3">
            <div className="flex items-center text-sm">
              <FiStar className="text-yellow-400 mr-1 fill-current" />
              <span className="font-medium">{book.rating?.toFixed(1) || '0.0'}</span>
            </div>
            <div className="flex items-center text-sm text-gray-400">
              <FiEye className="mr-1" />
              <span>{book.views || 0}</span>
            </div>
          </div>
          
          <div className="flex items-center text-sm text-gray-400">
            <FiHeart className={`mr-1 ${book.isLikedByUser ? 'text-red-500 fill-current' : ''}`} />
            <span>{book.likesCount || 0}</span>
          </div>
        </div>
      </div>
    </Link>
  );
};

export default BookCard;
