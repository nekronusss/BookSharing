import React, { useState, useEffect } from 'react';
import { userAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { FiUser, FiMail, FiLock, FiUpload, FiSave, FiSettings, FiShield } from 'react-icons/fi';
import Button from '../components/ui/Button';
import Badge from '../components/ui/Badge';

const Profile = () => {
  const { user, logout } = useAuth();
  const [profile, setProfile] = useState({
    displayName: '',
    email: '',
    avatarUrl: '',
  });
  const [privacy, setPrivacy] = useState({
    privateProfile: false,
    privateBooks: false,
  });
  const [avatar, setAvatar] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [status, setStatus] = useState({ type: '', message: '' });

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      const response = await userAPI.getProfile();
      const data = response.data;
      setProfile({
        displayName: data.displayName || '',
        email: data.email || '',
        avatarUrl: data.avatarUrl || '',
      });
      setPrivacy({
        privateProfile: data.privateProfile || false,
        privateBooks: data.privateBooks || false,
      });
    } catch (error) {
      console.error('Error loading profile:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setProfile((prev) => ({ ...prev, [name]: value }));
  };

  const handlePrivacyChange = (e) => {
    const { name, checked } = e.target;
    setPrivacy((prev) => ({ ...prev, [name]: checked }));
  };

  const handleAvatarChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setAvatar(file);
    }
  };

  const handleSave = async () => {
    setSaving(true);
    setStatus({ type: '', message: '' });
    try {
      await userAPI.updateProfile(profile, avatar);
      await userAPI.updatePrivacy(privacy);
      setStatus({ type: 'success', message: 'Profile updated successfully!' });
      loadProfile();
      setAvatar(null);
    } catch (error) {
      setStatus({ type: 'error', message: error.formattedMessage || 'Error updating profile' });
    } finally {
      setSaving(false);
    }
  };

  const getAvatarUrl = () => {
    if (avatar) {
      return URL.createObjectURL(avatar);
    }
    if (profile.avatarUrl) {
      if (profile.avatarUrl.startsWith('http')) return profile.avatarUrl;
      const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
      return `${API_BASE_URL}${profile.avatarUrl}`;
    }
    return null;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      <div className="mb-8">
        <h1 className="text-3xl font-extrabold text-gray-900">Account Settings</h1>
        <p className="text-gray-500 mt-1">Manage your public profile and privacy preferences</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
        {/* Sidebar Nav */}
        <div className="lg:col-span-1 space-y-2">
          <button className="w-full flex items-center px-4 py-2 text-sm font-medium rounded-lg bg-primary-50 text-primary-700">
            <FiUser className="mr-3" /> Profile Info
          </button>
          <button className="w-full flex items-center px-4 py-2 text-sm font-medium rounded-lg text-gray-600 hover:bg-gray-50">
            <FiShield className="mr-3" /> Security
          </button>
          <button className="w-full flex items-center px-4 py-2 text-sm font-medium rounded-lg text-gray-600 hover:bg-gray-50">
            <FiSettings className="mr-3" /> Preferences
          </button>
        </div>

        {/* Main Content */}
        <div className="lg:col-span-3 space-y-8">
          {status.message && (
            <div
              className={`rounded-xl p-4 flex items-center gap-3 animate-in fade-in slide-in-from-top-2 ${
                status.type === 'error'
                  ? 'bg-red-50 text-red-800 border border-red-100'
                  : 'bg-green-50 text-green-800 border border-green-100'
              }`}
            >
              {status.type === 'success' ? <FiCheck /> : <FiAlertCircle />}
              <p className="text-sm font-medium">{status.message}</p>
            </div>
          )}

          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="p-8 space-y-10">
              {/* Avatar Section */}
              <section>
                <h2 className="text-sm font-bold text-gray-900 uppercase tracking-widest mb-6">Profile Picture</h2>
                <div className="flex flex-col sm:flex-row items-center gap-8">
                  <div className="relative group">
                    <div className="h-32 w-32 rounded-full ring-4 ring-gray-50 overflow-hidden bg-gray-100 flex items-center justify-center">
                      {getAvatarUrl() ? (
                        <img
                          src={getAvatarUrl()}
                          alt="Avatar"
                          className="h-full w-full object-cover"
                        />
                      ) : (
                        <FiUser className="h-16 w-16 text-gray-300" />
                      )}
                    </div>
                    <label
                      htmlFor="avatar-upload"
                      className="absolute inset-0 flex items-center justify-center bg-black bg-opacity-40 text-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer"
                    >
                      <FiUpload className="h-6 w-6" />
                    </label>
                  </div>
                  <div className="text-center sm:text-left">
                    <input
                      id="avatar-upload"
                      type="file"
                      accept="image/*"
                      className="hidden"
                      onChange={handleAvatarChange}
                    />
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => document.getElementById('avatar-upload').click()}
                    >
                      {avatar ? 'Replace Image' : 'Change Avatar'}
                    </Button>
                    <p className="mt-2 text-xs text-gray-400">
                      JPG, PNG or GIF. Max size 2MB.
                    </p>
                  </div>
                </div>
              </section>

              {/* Information Section */}
              <section>
                <h2 className="text-sm font-bold text-gray-900 uppercase tracking-widest mb-6">General Information</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-gray-700">Username</label>
                    <div className="px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-gray-500 text-sm select-none">
                      {user?.username}
                    </div>
                    <p className="text-[10px] text-gray-400">Usernames cannot be changed.</p>
                  </div>

                  <div className="space-y-2">
                    <label htmlFor="displayName" className="text-sm font-medium text-gray-700">Display Name</label>
                    <input
                      type="text"
                      id="displayName"
                      name="displayName"
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all text-sm"
                      value={profile.displayName}
                      onChange={handleChange}
                      placeholder="Enter your name"
                    />
                  </div>

                  <div className="md:col-span-2 space-y-2">
                    <label htmlFor="email" className="text-sm font-medium text-gray-700">Email Address</label>
                    <div className="relative">
                      <FiMail className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                      <input
                        type="email"
                        id="email"
                        name="email"
                        className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all text-sm"
                        value={profile.email}
                        onChange={handleChange}
                      />
                    </div>
                  </div>
                </div>
              </section>

              {/* Privacy Section */}
              <section className="border-t border-gray-50 pt-10">
                <h2 className="text-sm font-bold text-gray-900 uppercase tracking-widest mb-6">Privacy & Safety</h2>
                <div className="space-y-4">
                  <label className="flex items-start gap-4 p-4 border border-gray-100 rounded-xl hover:bg-gray-50 transition-colors cursor-pointer">
                    <input
                      name="privateProfile"
                      type="checkbox"
                      className="mt-1 h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                      checked={privacy.privateProfile}
                      onChange={handlePrivacyChange}
                    />
                    <div>
                      <span className="block text-sm font-bold text-gray-900">Private Profile</span>
                      <span className="block text-xs text-gray-500 mt-1">
                        Only your followers can see your profile details and exchange history.
                      </span>
                    </div>
                  </label>

                  <label className="flex items-start gap-4 p-4 border border-gray-100 rounded-xl hover:bg-gray-50 transition-colors cursor-pointer">
                    <input
                      name="privateBooks"
                      type="checkbox"
                      className="mt-1 h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                      checked={privacy.privateBooks}
                      onChange={handlePrivacyChange}
                    />
                    <div>
                      <span className="block text-sm font-bold text-gray-900">Hide Collection</span>
                      <span className="block text-xs text-gray-500 mt-1">
                        Your library will be hidden from the public catalog.
                      </span>
                    </div>
                  </label>
                </div>
              </section>
            </div>

            <div className="p-8 bg-gray-50 border-t border-gray-100 flex justify-end gap-4">
              <Button variant="outline" onClick={loadProfile} disabled={saving}>
                Discard Changes
              </Button>
              <Button onClick={handleSave} loading={saving}>
                <FiSave className="mr-2" />
                Save Preferences
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;







