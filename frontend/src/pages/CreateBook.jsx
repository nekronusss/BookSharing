import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { bookAPI } from '../services/api';
import { FiUpload, FiX, FiCheckCircle } from 'react-icons/fi';
import Button from '../components/ui/Button';

const CreateBook = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [formData, setFormData] = useState({
    title: '',
    author: '',
    year: new Date().getFullYear(),
    description: '',
    category: '',
    tags: '',
    isPublic: true,
  });
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const bookData = {
        ...formData,
        tags: formData.tags
          ? formData.tags.split(',').map((tag) => tag.trim())
          : [],
      };

      await bookAPI.create(bookData, file);
      const returnTo = location.state?.returnTo;
      navigate(returnTo || '/books');
    } catch (err) {
      setError(err.formattedMessage || 'Error creating book');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="p-8 border-b border-gray-50 bg-gray-50/50">
          <h1 className="text-3xl font-extrabold text-gray-900">Add New Book</h1>
          <p className="text-gray-500 mt-1">Fill in the details to add a book to your collection</p>
        </div>

        <div className="p-8">
          {error && (
            <div className="mb-6 flex items-center gap-3 rounded-xl bg-red-50 p-4 border border-red-100 text-red-800 text-sm font-medium animate-in fade-in slide-in-from-top-2">
              <FiX className="flex-shrink-0" />
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-8">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              <div className="space-y-2">
                <label htmlFor="title" className="text-sm font-bold text-gray-700">Book Title *</label>
                <input
                  type="text"
                  id="title"
                  name="title"
                  required
                  placeholder="e.g. The Great Gatsby"
                  className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all"
                  value={formData.title}
                  onChange={handleChange}
                />
              </div>

              <div className="space-y-2">
                <label htmlFor="author" className="text-sm font-bold text-gray-700">Author *</label>
                <input
                  type="text"
                  id="author"
                  name="author"
                  required
                  placeholder="e.g. F. Scott Fitzgerald"
                  className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all"
                  value={formData.author}
                  onChange={handleChange}
                />
              </div>

              <div className="space-y-2">
                <label htmlFor="year" className="text-sm font-bold text-gray-700">Year *</label>
                <input
                  type="number"
                  id="year"
                  name="year"
                  required
                  min="1000"
                  max={new Date().getFullYear() + 1}
                  className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all"
                  value={formData.year}
                  onChange={handleChange}
                />
              </div>

              <div className="space-y-2">
                <label htmlFor="category" className="text-sm font-bold text-gray-700">Category</label>
                <input
                  type="text"
                  id="category"
                  name="category"
                  placeholder="e.g. Fiction, Classics"
                  className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all"
                  value={formData.category}
                  onChange={handleChange}
                />
              </div>
            </div>

            <div className="space-y-2">
              <label htmlFor="description" className="text-sm font-bold text-gray-700">Description</label>
              <textarea
                id="description"
                name="description"
                rows={4}
                placeholder="Tell others about this book..."
                className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all resize-none"
                value={formData.description}
                onChange={handleChange}
              />
            </div>

            <div className="space-y-2">
              <label htmlFor="tags" className="text-sm font-bold text-gray-700">Tags</label>
              <input
                type="text"
                id="tags"
                name="tags"
                placeholder="fiction, adventure, classic (separated by commas)"
                className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all"
                value={formData.tags}
                onChange={handleChange}
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-bold text-gray-700">Cover Image</label>
              <div className={`mt-1 flex flex-col items-center justify-center px-6 py-10 border-2 border-dashed rounded-2xl transition-all ${
                file ? 'border-primary-400 bg-primary-50' : 'border-gray-300 hover:border-primary-400 hover:bg-gray-50'
              }`}>
                {file ? (
                  <div className="text-center">
                    <FiCheckCircle className="mx-auto h-12 w-12 text-primary-500 mb-2" />
                    <p className="text-sm font-bold text-primary-900">{file.name}</p>
                    <button 
                      type="button" 
                      onClick={() => setFile(null)}
                      className="mt-2 text-xs text-red-600 font-bold hover:underline"
                    >
                      Remove File
                    </button>
                  </div>
                ) : (
                  <div className="text-center">
                    <FiUpload className="mx-auto h-12 w-12 text-gray-400 mb-2" />
                    <div className="flex text-sm text-gray-600">
                      <label htmlFor="file-upload" className="relative cursor-pointer font-bold text-primary-600 hover:text-primary-500 underline">
                        <span>Upload a file</span>
                        <input id="file-upload" name="file-upload" type="file" className="sr-only" accept="image/*" onChange={handleFileChange} />
                      </label>
                      <p className="pl-1">or drag and drop</p>
                    </div>
                    <p className="text-xs text-gray-500 mt-1">PNG, JPG up to 10MB</p>
                  </div>
                )}
              </div>
            </div>

            <label className="flex items-center gap-3 cursor-pointer group">
              <input
                id="isPublic"
                name="isPublic"
                type="checkbox"
                className="h-5 w-5 text-primary-600 focus:ring-primary-500 border-gray-300 rounded-lg transition-all"
                checked={formData.isPublic}
                onChange={handleChange}
              />
              <span className="text-sm font-medium text-gray-700 group-hover:text-gray-900 transition-colors">
                Make this book visible in the public catalog
              </span>
            </label>

            <div className="flex justify-end gap-4 pt-4 border-t border-gray-50">
              <Button variant="outline" onClick={() => navigate('/books')} disabled={loading}>
                Cancel
              </Button>
              <Button type="submit" loading={loading} className="px-10">
                List Book
              </Button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default CreateBook;





