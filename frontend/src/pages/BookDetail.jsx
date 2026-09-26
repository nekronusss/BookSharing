import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { bookAPI, commentAPI, ratingAPI } from '../services/api';
import { FiStar, FiEye, FiHeart, FiTrash2, FiEdit, FiSend, FiRefreshCw } from 'react-icons/fi';
import QRCodeDisplay from '../components/QRCodeDisplay';
import Button from '../components/ui/Button';
import Badge from '../components/ui/Badge';
import ConfirmModal from '../components/ui/ConfirmModal';

const BookDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [book, setBook] = useState(null);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [avgRating, setAvgRating] = useState(0);
  const [userScore, setUserScore] = useState(0);
  const [pendingScore, setPendingScore] = useState(0);
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isRatingSubmitting, setIsRatingSubmitting] = useState(false);
  const [liked, setLiked] = useState(false);
  const [loading, setLoading] = useState(true);
  const [userRating, setUserRating] = useState(null);
  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editingText, setEditingText] = useState('');
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [commentToDelete, setCommentToDelete] = useState(null);
  const [isDeletingBook, setIsDeletingBook] = useState(false);
  const [isDeletingComment, setIsDeletingComment] = useState(false);

  useEffect(() => {
    loadBook();
    loadComments();
  }, [id]);

  const loadBook = async () => {
    try {
      const response = await bookAPI.getById(id);
      const bookData = response.data;
      setBook(bookData);
      setAvgRating(bookData.rating || 0);
      setLiked(bookData.isLikedByUser || false);
    } catch (error) {
      console.error('Error loading book:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadComments = async () => {
    try {
      const response = await commentAPI.getByBook(id);
      setComments(response.data);
    } catch (error) {
      console.error('Error loading comments:', error);
    }
  };

  const handleDelete = async () => {
    setIsDeletingBook(true);
    try {
      await bookAPI.delete(id);
      setIsDeleteModalOpen(false);
      navigate('/books');
    } catch (error) {
      alert('Error deleting book');
    } finally {
      setIsDeletingBook(false);
    }
  };

  const handleAddComment = async () => {
    if (!newComment.trim()) return;
    try {
      await commentAPI.create(id, newComment);
      setNewComment('');
      loadComments();
    } catch (error) {
      const message =
        error?.response?.data?.message ||
        (typeof error?.response?.data === 'string' ? error.response.data : null) ||
        error?.message ||
        'Error adding comment';
      console.error('Error adding comment:', error?.response?.status, error?.response?.data || error);
      alert(message);
    }
  };

  const handleDeleteComment = async () => {
    if (!commentToDelete) return;
    setIsDeletingComment(true);
    try {
      await commentAPI.delete(commentToDelete);
      setCommentToDelete(null);
      loadComments();
    } catch (error) {
      alert('Error deleting comment');
    } finally {
      setIsDeletingComment(false);
    }
  };

  const handleUpdateComment = async (commentId) => {
    if (!editingText.trim()) return;
    try {
      await commentAPI.update(commentId, editingText);
      setEditingCommentId(null);
      setEditingText('');
      loadComments();
    } catch (error) {
      alert('Error updating comment');
    }
  };

  const handleRate = async (score, isLiked) => {
    try {
      // Optimistic update to make the UI react instantly.
      setIsRatingSubmitting(true);
      setUserScore(score);
      const response = await ratingAPI.rate(id, score, isLiked);
      if (response?.data?.score != null) {
        setUserScore(response.data.score);
      }
      loadBook();
    } catch (error) {
      // Revert optimistic update if request fails.
      setUserScore(0);
      alert('Error rating book');
    } finally {
      setIsRatingSubmitting(false);
    }
  };

  const openConfirmRating = (score) => {
    setPendingScore(score);
    setIsConfirmModalOpen(true);
  };

  const confirmRating = async () => {
    if (!pendingScore) return;
    setIsConfirmModalOpen(false);
    await handleRate(pendingScore, liked);
    setPendingScore(0);
  };

  const openEditRating = () => {
    setPendingScore(userScore || 0);
    setIsEditModalOpen(true);
  };

  const saveEditedRating = async () => {
    if (!pendingScore) return;
    setIsEditModalOpen(false);
    await handleRate(pendingScore, liked);
    setPendingScore(0);
  };

  const removeMyRating = async () => {
    try {
      setIsRatingSubmitting(true);
      await ratingAPI.remove(id);
      setUserScore(0);
      setPendingScore(0);
      setIsEditModalOpen(false);
      await loadBook();
    } catch (error) {
      alert('Error removing rating');
    } finally {
      setIsRatingSubmitting(false);
    }
  };

  const handleToggleLike = async () => {
    if (!user) {
      alert('Please login to like books');
      return;
    }
    try {
      const response = await bookAPI.toggleLike(id);
      const bookData = response.data;
      // Update the book state with the response
      setBook(bookData);
      setLiked(bookData.isLikedByUser || false);
    } catch (error) {
      console.error('Error toggling like:', error);
      alert('Error toggling like. Please try again.');
    }
  };

  const getImageUrl = (imageUrl) => {
    if (!imageUrl) return 'https://via.placeholder.com/400x600?text=No+Image';
    if (imageUrl.startsWith('http')) return imageUrl;
    const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
    return `${API_BASE_URL}${imageUrl}`;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (!book) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-500 text-lg">Book not found</p>
      </div>
    );
  }

  const isOwner = user && book.userId && user.username === book.username;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="bg-white rounded-lg shadow-lg overflow-hidden">
        <div className="md:flex">
          <div className="md:w-1/3">
            <img
              src={getImageUrl(book.imageUrl)}
              alt={book.title}
              className="w-full h-full object-cover"
            />
          </div>
          <div className="md:w-2/3 p-8">
            <div className="flex justify-between items-start mb-4">
              <div>
                <h1 className="text-3xl font-bold text-gray-900 mb-2">{book.title}</h1>
                <p className="text-xl text-gray-600 mb-4">by {book.author}</p>
              </div>
              {isOwner && (
                <div className="flex space-x-2">
                  <button
                    onClick={() => navigate(`/books/${id}/edit`)}
                    className="p-2 text-gray-600 hover:text-primary-600"
                  >
                    <FiEdit className="h-5 w-5" />
                  </button>
                  <button
                    onClick={() => setIsDeleteModalOpen(true)}
                    className="p-2 text-gray-600 hover:text-red-600"
                  >
                    <FiTrash2 className="h-5 w-5" />
                  </button>
                </div>
              )}
            </div>

            <div className="flex items-center space-x-6 mb-4">
              <div className="flex items-center">
                <FiStar className="text-yellow-400 mr-1" />
                <span className="font-semibold">{avgRating?.toFixed(1) || '0.0'}</span>
              </div>
              <div className="flex items-center">
                <FiEye className="mr-1" />
                <span>{book.views || 0} views</span>
              </div>
              <button
                onClick={handleToggleLike}
                className={`flex items-center transition-colors ${
                  liked ? 'text-red-500 hover:text-red-600' : 'text-gray-500 hover:text-red-400'
                }`}
              >
                <FiHeart className={`mr-1 ${liked ? 'fill-current' : ''}`} />
                <span>{book.likesCount || 0}</span>
              </button>
            </div>

            {book.category && (
              <Badge variant="primary" className="mb-4">
                {book.category}
              </Badge>
            )}

            <div className="mb-6">
              <p className="text-gray-700 whitespace-pre-wrap">{book.description}</p>
            </div>

            <div className="mb-6">
              <p className="text-sm text-gray-500">
                <strong>Year:</strong> {book.year || 'N/A'}
              </p>
              {book.addedDate && (
                <p className="text-sm text-gray-500">
                  <strong>Added:</strong> {new Date(book.addedDate).toLocaleDateString()}
                </p>
              )}
              {book.username && (
                <p className="text-sm text-gray-500">
                  <strong>Shared by:</strong> {book.username}
                </p>
              )}
            </div>

            {book.tags && book.tags.length > 0 && (
              <div className="mb-6">
                <div className="flex flex-wrap gap-2">
                  {book.tags.map((tag, index) => (
                    <span
                      key={index}
                      className="px-2 py-1 text-xs font-medium bg-gray-100 text-gray-800 rounded"
                    >
                      #{tag}
                    </span>
                  ))}
                </div>
              </div>
            )}

            {user && !isOwner && (
              <div className="border-t pt-6 mb-6">
                <Button
                  onClick={() => navigate(`/exchanges/create/${book.id}`)}
                  className="w-full py-3"
                >
                  <FiRefreshCw className="mr-2" />
                  Request Exchange
                </Button>
              </div>
            )}

            {user && (
              <div className="border-t pt-6">
                <h3 className="text-lg font-semibold mb-4">Rate this book</h3>
                <div className="flex items-center space-x-4 mb-4">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button
                      key={star}
                      type="button"
                      disabled={isRatingSubmitting || userScore > 0}
                      onClick={() => openConfirmRating(star)}
                      className={`text-2xl transition-colors ${
                        star <= userScore ? 'text-yellow-400' : 'text-gray-300'
                      } ${isRatingSubmitting || userScore > 0 ? 'cursor-not-allowed opacity-70' : 'hover:text-yellow-500'}`}
                    >
                      <FiStar fill={star <= userScore ? 'currentColor' : 'none'} />
                    </button>
                  ))}
                </div>
                <p className="text-sm text-gray-500">
                  Your score: {userScore || 'not rated yet'} · Average: {avgRating?.toFixed(1) || '0.0'}
                </p>

                {userScore > 0 && (
                  <div className="mt-4">
                    <button
                      type="button"
                      onClick={openEditRating}
                      className="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors"
                    >
                      Change rating
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* QR Code Section */}
      {book.qrCode && (
        <div className="mt-8">
          <QRCodeDisplay bookId={book.id} qrCode={book.qrCode} title={book.title} />
        </div>
      )}

      {/* Modals */}
      <ConfirmModal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        onConfirm={handleDelete}
        title="Delete Book"
        message={`Are you sure you want to delete "${book.title}"? This action cannot be undone.`}
        loading={isDeletingBook}
      />

      <ConfirmModal
        isOpen={!!commentToDelete}
        onClose={() => setCommentToDelete(null)}
        onConfirm={handleDeleteComment}
        title="Delete Comment"
        message="Are you sure you want to delete this comment?"
        loading={isDeletingComment}
      />

      {/* Comments Section */}
      <div className="mt-8 bg-white rounded-lg shadow-lg p-8">
        <h2 className="text-2xl font-bold mb-6">Comments</h2>
        
        {user && (
          <div className="mb-6">
            <div className="flex space-x-2">
              <input
                type="text"
                placeholder="Add a comment..."
                className="flex-1 px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-primary-500"
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleAddComment()}
              />
              <button
                onClick={handleAddComment}
                className="px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700"
              >
                <FiSend />
              </button>
            </div>
          </div>
        )}

        <div className="space-y-4">
          {comments.length === 0 ? (
            <p className="text-gray-500">No comments yet</p>
          ) : (
            comments.map((comment) => (
              <div key={comment.id} className="border-b pb-4">
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <p className="font-semibold">{comment.authorUsername || 'Anonymous'}</p>
                    {editingCommentId === comment.id ? (
                      <div className="mt-2">
                        <textarea
                          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-primary-500"
                          value={editingText}
                          onChange={(e) => setEditingText(e.target.value)}
                          rows="3"
                        />
                        <div className="flex space-x-2 mt-2">
                          <button
                            onClick={() => handleUpdateComment(comment.id)}
                            className="px-3 py-1 bg-primary-600 text-white text-sm rounded-md hover:bg-primary-700"
                          >
                            Save
                          </button>
                          <button
                            onClick={() => {
                              setEditingCommentId(null);
                              setEditingText('');
                            }}
                            className="px-3 py-1 bg-gray-200 text-gray-700 text-sm rounded-md hover:bg-gray-300"
                          >
                            Cancel
                          </button>
                        </div>
                      </div>
                    ) : (
                      <>
                        <p className="text-gray-700">{comment.text}</p>
                        {comment.createdAt && (
                          <p className="text-sm text-gray-500 mt-1">
                            {new Date(comment.createdAt).toLocaleString()}
                          </p>
                        )}
                      </>
                    )}
                  </div>
                  {user && user.username === comment.authorUsername && editingCommentId !== comment.id && (
                    <div className="flex space-x-2 ml-4">
                      <button
                        onClick={() => {
                          setEditingCommentId(comment.id);
                          setEditingText(comment.text);
                        }}
                        className="p-1 text-gray-400 hover:text-primary-600"
                        title="Edit comment"
                      >
                        <FiEdit className="h-4 w-4" />
                      </button>
                      <button
                        onClick={() => setCommentToDelete(comment.id)}
                        className="p-1 text-gray-400 hover:text-red-600"
                        title="Delete comment"
                      >
                        <FiTrash2 className="h-4 w-4" />
                      </button>
                    </div>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Confirm Rating Modal */}
      {isConfirmModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div className="w-full max-w-md bg-white rounded-lg shadow-xl p-6">
            <h4 className="text-lg font-semibold text-gray-900 mb-2">Confirm rating</h4>
            <p className="text-sm text-gray-600 mb-4">
              Are you sure you want to set <span className="font-semibold">{pendingScore}</span> star(s)?
            </p>

            <div className="flex items-center justify-end space-x-3">
              <button
                type="button"
                onClick={() => setIsConfirmModalOpen(false)}
                className="px-4 py-2 text-sm rounded-md border border-gray-300 text-gray-700 hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={confirmRating}
                disabled={isRatingSubmitting}
                className="px-4 py-2 text-sm rounded-md bg-primary-600 text-white hover:bg-primary-700 disabled:opacity-70"
              >
                Yes, set rating
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Edit Rating Modal */}
      {isEditModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div className="w-full max-w-md bg-white rounded-lg shadow-xl p-6">
            <h4 className="text-lg font-semibold text-gray-900 mb-2">Change rating</h4>
            <p className="text-sm text-gray-600 mb-4">Select a new rating and save it.</p>

            <div className="flex items-center space-x-3 mb-6">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  key={star}
                  type="button"
                  onClick={() => setPendingScore(star)}
                  className={`text-3xl transition-colors ${
                    star <= pendingScore ? 'text-yellow-400' : 'text-gray-300'
                  } hover:text-yellow-500`}
                >
                  <FiStar fill={star <= pendingScore ? 'currentColor' : 'none'} />
                </button>
              ))}
            </div>

            <div className="flex items-center justify-between">
              <p className="text-sm text-gray-500">New score: {pendingScore || '—'}</p>
              <div className="flex items-center space-x-3">
                <button
                  type="button"
                  onClick={removeMyRating}
                  disabled={isRatingSubmitting}
                  className="px-4 py-2 text-sm rounded-md border border-red-300 text-red-700 hover:bg-red-50 disabled:opacity-70"
                >
                  Remove rating
                </button>
                <button
                  type="button"
                  onClick={() => setIsEditModalOpen(false)}
                  className="px-4 py-2 text-sm rounded-md border border-gray-300 text-gray-700 hover:bg-gray-50"
                >
                  Close
                </button>
                <button
                  type="button"
                  onClick={saveEditedRating}
                  disabled={!pendingScore || isRatingSubmitting}
                  className="px-4 py-2 text-sm rounded-md bg-primary-600 text-white hover:bg-primary-700 disabled:opacity-70"
                >
                  Save
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default BookDetail;


