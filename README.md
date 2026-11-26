# Spring Boot Authentication System

A secure authentication and authorization backend built using **Java, Spring Boot, Spring Security, and JWT**. This project provides user registration, login, token-based authentication, Google OAuth2 login, password recovery, and role-based authorization.

## 🚀 Features

- **User Registration** – Register new users with email and password.
- **User Login** – Authenticate users using Spring Security.
- **JWT Authentication** – Generate and validate access tokens.
- **Refresh Token** – Generate new access tokens using refresh tokens.
- **Secure Cookie Management** – Store refresh tokens in HttpOnly cookies.
- **Role-Based Authorization** – Manage user roles and restrict access to protected endpoints.
- **Google OAuth2 Login** – Allow users to authenticate using their Google accounts.
- **Forgot Password** – Send password reset links through email.
- **Reset Password** – Securely reset passwords using time-limited, one-time tokens.
- **Password Encryption** – Hash passwords using BCrypt.
- **Token Revocation** – Revoke refresh tokens during logout.
- **Bean Validation** – Validate incoming request data.
- **Global Exception Handling** – Handle application errors and validation exceptions consistently.

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java | Backend programming |
| Spring Boot | Application framework |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Database operations |
| Hibernate | ORM |
| MySQL | Database |
| JWT | Token-based authentication |
| OAuth2 / OIDC | Google authentication |
| BCrypt | Password hashing |
| Maven | Dependency management |
| Jakarta Bean Validation | Request validation |
| Postman | API testing |

## 📂 Project Structure

```text
src/
 └── main/
     ├── java/
     │   └── com/example/
     │       ├── config/
     │       ├── controller/
     │       ├── dtos/
     │       ├── entities/
     │       ├── exception/
     │       ├── helper/
     │       ├── repository/
     │       ├── security/
     │       ├── service/
     │       └── Application.java
     │
     └── resources/
         ├── static/
         ├── templates/
         ├── application.yaml
         └── application-dev.yaml
```


## 🔗 API Endpoints

Base URL:

```text
http://localhost:8080/api/v1/users
```

| Method | Endpoint | Description |
|---|---|---|
| POST | `/register` | Register a new user |
| POST | `/login` | Login with email and password |
| POST | `/refresh` | Generate a new access token |
| POST | `/logout` | Logout and revoke refresh token |
| POST | `/forgot-password` | Request a password reset email |
| POST | `/reset-password` | Reset password using a token |
| GET | `/profile` | Access user profile |

Google OAuth2 login is also supported.

## 🔐 Security

This project implements multiple security mechanisms:

- BCrypt password hashing to protect stored passwords.
- JWT-based stateless authentication.
- Separate access and refresh tokens.
- Refresh token storage using HttpOnly cookies.
- Refresh token validation and revocation.
- Role-based endpoint authorization.
- Google OAuth2 authentication.
- Secure, time-limited password reset tokens.
- Input validation and centralized exception handling.

## 🧪 API Testing

The APIs can be tested using **Postman**.

Test the following workflows:

1. Register a new user.
2. Login and receive an access token.
3. Access protected endpoints.
4. Refresh the access token.
5. Logout and revoke the refresh token.
6. Login using Google.
7. Request a password reset email.
8. Reset the password using the reset token.

## 📌 Future Improvements

- Add automated unit and integration tests using JUnit and Mockito.
- Add API documentation using Swagger / OpenAPI.
- Add Docker support for easier deployment.
- integration front end for it.

## 👨‍💻 Author

**YOUR NAME**

Java Backend Developer

GitHub: https://github.com/YOUR_USERNAME
