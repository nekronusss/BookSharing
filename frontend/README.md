# Book Sharing Frontend

A modern React frontend for the Book Sharing application.

## Features

- 🔐 User Authentication (Login/Register)
- 📚 Book Browsing and Search
- ➕ Create and Manage Books
- ⭐ Rate and Comment on Books
- ❤️ Favorites System
- 👤 User Profile Management
- 🎨 Modern UI with Tailwind CSS

## Tech Stack

- **React 18** - UI library
- **Vite** - Build tool
- **React Router** - Routing
- **Axios** - HTTP client
- **Tailwind CSS** - Styling
- **React Icons** - Icons

## Getting Started

### Prerequisites

- Node.js 16+ and npm/yarn
- Backend API running on `http://localhost:8080`

### Installation

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm run dev
```

3. Open your browser to `http://localhost:4200`

### Building for Production

```bash
npm run build
```

The built files will be in the `dist` directory.

## Project Structure

```
frontend/
├── src/
│   ├── components/      # Reusable components
│   ├── pages/           # Page components
│   ├── context/         # React context providers
│   ├── services/        # API services
│   └── App.jsx          # Main app component
├── public/              # Static assets
└── package.json         # Dependencies
```

## API Configuration

The frontend is configured to connect to the backend API at `http://localhost:8080`. 
You can change this in `src/services/api.js` if needed.

## Features Overview

### Authentication
- User registration and login
- JWT token-based authentication
- Protected routes

### Books
- Browse all books
- Search and filter books
- View book details
- Create new books
- Edit/delete your own books
- Upload book covers/images

### Social Features
- Rate books (1-5 stars)
- Like books
- Comment on books
- View favorites

### User Profile
- Update profile information
- Upload avatar
- Privacy settings

## Development

The app uses Vite for fast development with hot module replacement (HMR).

## License

Same as the main project.



