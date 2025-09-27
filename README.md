# 📚 Book Sharing System with Users

## Project description
**Book Sharing System** is a web application for sharing books between users.  
Each book has a unique QR code and can be “rented” by another user.  
The project implements a simple book catalog with basic CRUD operations, plus a user system for authentication and authorization.

---

## Main functions

- Adding a new book (POST, only for authenticated users)
- Getting a list of all books (GET)
- Getting a book by ID (GET)
- Deleting a book by ID (DELETE, only own books)
- Updating information for books by ID (PUT, only own books)
- User registration (POST /register)
- User login (POST /login)
- Getting current user info (GET /me)

---

## Stack used

- **Java 21+**
- **Spring Boot**
- **Spring Data JPA**
- **PostgreSQL**
- **Spring Security + JWT**

---

## ⚙️ Launch instruction

### 1. User registration
**POST /auth/register for 2 users** examples↓
```json
{
  "username": "bob",
  "password": "bob123"
}
```

```json
{
  "username": "alice",
  "password": "alice123"
}
```

### 2. User login
**POST /auth/login for users** examples↓
```json
{
  "username": "bob",
  "password": "bob123"
}
```

```json
{
  "username": "alice",
  "password": "alice123"
}
```
### 3. User info GET /me and books info /books

### 4. Creating a books POST /books *examples*↓
```json
{
  "title": "Bob's Guide",
  "author": "Bob Writer",
  "year": 2020,
  "description": "Programming guide",
  "qrCode": "QR123Bob"
}
```

```json
{
"title": "Alice Adventures",
"author": "Alice Writer",
"year": 2021,
"description": "A guide to adventures",
"qrCode": "QR123Alice"
}
```

### 5. Book info updating PUT /books/{id} *examples*↓
```json
{
  "title": "Bob's Guide",
  "author": "Bob Writer",
  "year": 2020,
  "description": "Programming guide",
  "qrCode": "QR123Bob ⇐ QR321bOB *changed*"
}
```

### 6. Books deleting DELETE /books/id *examples*↓
```json
"http://localhost:8080/books/{id} ⇐ example" 
```