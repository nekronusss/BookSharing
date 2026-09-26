import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { exchangeAPI, bookAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { FiArrowLeft, FiAlertCircle, FiPlus } from 'react-icons/fi';

const CreateExchange = () => {
  const { bookId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const [requestedBook, setRequestedBook] = useState(null);
  const [myBooks, setMyBooks] = useState([]);
  const [availableBooks, setAvailableBooks] = useState([]);
  const [formData, setFormData] = useState({
    requestedBookId: bookId || '',
    offeredBookId: '',
    message: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [loadingBooks, setLoadingBooks] = useState(true);

  useEffect(() => {
    if (bookId) {
      loadRequestedBook();
    } else {
      loadAvailableBooks();
    }
    loadMyBooks();
  }, [bookId, location.key, user]); // location.key changes when we navigate back, triggering reload

  const loadRequestedBook = async () => {
    try {
      const response = await bookAPI.getById(bookId);
      setRequestedBook(response.data);
      setFormData(prev => ({ ...prev, requestedBookId: bookId }));
    } catch (error) {
      console.error('Error loading book:', error);
      setError('Failed to load book details');
    }
  };

  const loadAvailableBooks = async () => {
    try {
      const response = await bookAPI.getAll();
      // Filter out books owned by the current user (they can't request their own books)
      const booksToRequest = response.data.filter(book => book.username !== user?.username);
      setAvailableBooks(booksToRequest);
    } catch (error) {
      console.error('Error loading available books:', error);
    }
  };

  const loadMyBooks = async () => {
    try {
      const response = await bookAPI.getAll();
      // Filter to only show books owned by the current user (compare by username)
      const userBooks = response.data.filter(book => book.username === user?.username);
      setMyBooks(userBooks);
    } catch (error) {
      console.error('Error loading books:', error);
    } finally {
      setLoadingBooks(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await exchangeAPI.create(
        parseInt(formData.requestedBookId),
        formData.offeredBookId ? parseInt(formData.offeredBookId) : null,
        formData.message || null
      );
      navigate('/exchanges');
    } catch (err) {
      setError(err.response?.data?.message || 'Error creating exchange request');
    } finally {
      setLoading(false);
    }
  };

  if (loadingBooks) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <button
        onClick={() => navigate(-1)}
        className="flex items-center text-gray-600 hover:text-gray-900 mb-6"
      >
        <FiArrowLeft className="mr-2" />
        Back
      </button>

      <div className="bg-white rounded-lg shadow-lg p-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-6">Create Exchange Request</h1>

        {error && (
          <div className="mb-4 rounded-md bg-red-50 p-4">
            <div className="flex">
              <FiAlertCircle className="h-5 w-5 text-red-400 mr-2" />
              <div className="text-sm text-red-800">{error}</div>
            </div>
          </div>
        )}

        {requestedBook && (
          <div className="mb-6 p-4 bg-blue-50 rounded-lg">
            <h3 className="text-sm font-medium text-blue-800 mb-2">Requesting Book:</h3>
            <p className="text-lg font-semibold text-blue-900">{requestedBook.title}</p>
            <p className="text-sm text-blue-700">by {requestedBook.author}</p>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          {!bookId && (
            <div>
              <label htmlFor="requestedBookId" className="block text-sm font-medium text-gray-700 mb-2">
                Book to Request *
              </label>
              <select
                id="requestedBookId"
                name="requestedBookId"
                required
                value={formData.requestedBookId}
                onChange={handleChange}
                className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-primary-500"
              >
                <option value="">Select a book...</option>
                {availableBooks.map((book) => (
                  <option key={book.id} value={book.id}>
                    {book.title} by {book.author}
                  </option>
                ))}
              </select>
              {availableBooks.length === 0 && !loadingBooks && (
                <p className="mt-1 text-sm text-gray-500">
                  No books available to request. Browse books to find one you'd like.
                </p>
              )}
            </div>
          )}

          <div>
            <label htmlFor="offeredBookId" className="block text-sm font-medium text-gray-700 mb-2">
              Book to Offer (Optional)
            </label>
            {myBooks.length === 0 ? (
              <div className="space-y-3">
                <div className="p-4 bg-yellow-50 border border-yellow-200 rounded-md">
                  <p className="text-sm text-yellow-800 mb-3">
                    You don't have any books to offer. You can either create an exchange without offering a book, or add a book first.
                  </p>
                  <button
                    type="button"
                    onClick={() => {
                      const returnPath = bookId 
                        ? `/exchanges/create/${bookId}` 
                        : '/exchanges/create';
                      navigate('/books/create', { state: { returnTo: returnPath } });
                    }}
                    className="inline-flex items-center px-4 py-2 bg-primary-600 text-white text-sm font-medium rounded-md hover:bg-primary-700 transition-colors"
                  >
                    <FiPlus className="mr-2" />
                    Add a Book to Offer
                  </button>
                </div>
                <input
                  type="hidden"
                  name="offeredBookId"
                  value=""
                />
              </div>
            ) : (
              <div className="space-y-2">
                <select
                  id="offeredBookId"
                  name="offeredBookId"
                  value={formData.offeredBookId}
                  onChange={handleChange}
                  className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-primary-500"
                >
                  <option value="">No book offered (one-way exchange)</option>
                  {myBooks.map((book) => (
                    <option key={book.id} value={book.id}>
                      {book.title} by {book.author}
                    </option>
                  ))}
                </select>
                <div className="flex items-center justify-between">
                  <p className="text-sm text-gray-500">
                    Select one of your books to offer in exchange (optional)
                  </p>
                  <button
                    type="button"
                    onClick={() => {
                      const returnPath = bookId 
                        ? `/exchanges/create/${bookId}` 
                        : '/exchanges/create';
                      navigate('/books/create', { state: { returnTo: returnPath } });
                    }}
                    className="inline-flex items-center text-sm text-primary-600 hover:text-primary-700 font-medium"
                  >
                    <FiPlus className="mr-1 h-4 w-4" />
                    Add New Book
                  </button>
                </div>
              </div>
            )}
          </div>

          <div>
            <label htmlFor="message" className="block text-sm font-medium text-gray-700 mb-2">
              Message (Optional)
            </label>
            <textarea
              id="message"
              name="message"
              rows="4"
              value={formData.message}
              onChange={handleChange}
              className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-primary-500"
              placeholder="Add a message to the book owner..."
            />
          </div>

          <div className="flex space-x-4">
            <button
              type="button"
              onClick={() => navigate(-1)}
              className="px-6 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading || !formData.requestedBookId}
              className="flex-1 px-6 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 disabled:opacity-50"
            >
              {loading ? 'Creating...' : 'Create Exchange Request'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateExchange;

