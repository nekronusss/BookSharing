import React, { useState, useEffect } from 'react';
import { bookAPI, API_BASE_URL } from '../services/api';
import BookCard from '../components/book/BookCard';
import SearchInput from '../components/ui/SearchInput';
import { FiFilter } from 'react-icons/fi';
import { useDebounce } from '../hooks/useDebounce';

const Books = () => {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searching, setSearching] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [category, setCategory] = useState('');
  const [tag, setTag] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  
  const debouncedSearchTerm = useDebounce(searchTerm, 500);
  const debouncedCategory = useDebounce(category, 500);
  const debouncedTag = useDebounce(tag, 500);

  const loadBooks = async (isInitial = false) => {
    if (isInitial) {
      setLoading(true);
    } else {
      setSearching(true);
    }
    
    try {
      const params = {};
      if (searchTerm.trim()) params.search = searchTerm.trim();
      if (category.trim()) params.category = category.trim();
      if (tag.trim()) params.tag = tag.trim();
      const response = await bookAPI.getAll(params);
      setBooks(response.data);
    } catch (error) {
      console.error('Error loading books:', error);
    } finally {
      setLoading(false);
      setSearching(false);
    }
  };

  useEffect(() => {
    loadBooks(true);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    // Only trigger if not loading (to avoid double call on initial mount)
    if (!loading) {
      loadBooks(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedSearchTerm, debouncedCategory, debouncedTag]);

  const getImageUrl = (imageUrl) => {
    if (!imageUrl) return 'https://via.placeholder.com/300x400?text=No+Image';
    if (imageUrl.startsWith('http')) return imageUrl;
    return `${API_BASE_URL}${imageUrl}`;
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
      <div className="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4">
        <div>
          <h1 className="text-3xl font-extrabold text-gray-900">Catalog</h1>
          <p className="text-gray-500 mt-1">Discover your next favorite book</p>
        </div>
        
        <div className="flex items-center gap-2">
          <div className="w-full md:w-80">
            <SearchInput
              value={searchTerm}
              onChange={setSearchTerm}
              placeholder="Search by title or author..."
              onClear={() => setSearchTerm('')}
            />
          </div>
          <button
            onClick={() => setShowFilters(!showFilters)}
            className={`p-2 rounded-lg border transition-colors ${
              showFilters || category || tag 
                ? 'bg-primary-50 border-primary-200 text-primary-600' 
                : 'bg-white border-gray-300 text-gray-600 hover:bg-gray-50'
            }`}
          >
            <FiFilter className="h-5 w-5" />
          </button>
        </div>
      </div>

      {/* Filters Panel */}
      {(showFilters || category || tag) && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-8 animate-in fade-in slide-in-from-top-2 duration-300">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Category</label>
              <input
                type="text"
                placeholder="e.g. Fiction, Science..."
                className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                value={category}
                onChange={(e) => setCategory(e.target.value)}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Tag</label>
              <input
                type="text"
                placeholder="e.g. classic, mystery..."
                className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                value={tag}
                onChange={(e) => setTag(e.target.value)}
              />
            </div>
          </div>
          {(category || tag) && (
            <div className="mt-4 flex justify-end">
              <button
                onClick={() => {
                  setCategory('');
                  setTag('');
                }}
                className="text-sm text-primary-600 hover:text-primary-700 font-medium"
              >
                Clear all filters
              </button>
            </div>
          )}
        </div>
      )}

      {searching && !loading && (
        <div className="mb-6 flex items-center text-sm text-primary-600 animate-pulse">
          <div className="h-2 w-2 bg-primary-600 rounded-full mr-2"></div>
          Updating results...
        </div>
      )}

      {/* Books Grid */}
      {books.length === 0 ? (
        <div className="bg-white rounded-2xl border-2 border-dashed border-gray-200 py-20 text-center">
          <p className="text-gray-400 text-lg">No books matching your criteria</p>
          <button
            onClick={() => {
              setSearchTerm('');
              setCategory('');
              setTag('');
            }}
            className="mt-4 text-primary-600 font-medium hover:underline"
          >
            Reset all filters
          </button>
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

export default Books;


