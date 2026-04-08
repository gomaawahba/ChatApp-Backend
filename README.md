ChatApp Backend

ChatApp Backend is the server-side service for the real-time messaging application ChatApp, built using Spring Boot, MongoDB, Redis, and WebSockets.
This project was developed incrementally while learning Docker, with features added gradually to improve functionality and architecture.

🛠️ Features
User Management
Register, login, and refresh authentication tokens (JWT).
Update profile information (display name and avatar).
Search for users by username.
Track user presence (online/offline) using Redis.
Conversations
Create direct and group conversations.
Add or remove members in group conversations (admin-only actions).
Track conversation metadata, including last message, unread count, and participants.
Messaging
Send, edit, delete, and react to messages.
Support for text and media messages.
Real-time updates via WebSockets.
Mark messages as read and notify participants.
Security
Authentication and authorization with Spring Security.
Passwords securely hashed using BCrypt.
Role-based access control for conversation operations.



🧰 Technologies Used
Java 17 + Spring Boot
MongoDB for persistent storage
Redis for user presence tracking
WebSockets for real-time communication
JWT for authentication
Postman for API testing
Swagger for API documentation
Docker for containerization

🚀 Installation
Clone the repository:
git clone <REPO_LINK>
cd chatapp-backend

Run MongoDB and Redis (via Docker or locally):
docker compose up -d
Build and run the Spring Boot application:
./mvnw clean install
./mvnw spring-boot:run
Access Swagger UI:
http://localhost:8080/swagger-ui.html

Project Structure
com.gomaa.chatapp
├── service           # Business logic for users, messages, conversations
├── repository        # Spring Data repositories
├── model             # Entities: User, Message, Conversation, etc.
├── dto               # Request and response objects
├── security          # JWT and authentication configuration
├── exception         # Custom exceptions
└── ChatAppApplication.java
