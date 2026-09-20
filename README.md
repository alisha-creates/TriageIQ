# TriageIQ

TriageIQ is an AI-powered ticket triage and support management system built using Spring Boot, Spring AI, Groq, Spring Security, MySQL, and WebSocket.

The system helps automatically analyze customer support tickets, classify them based on department and priority, provide confidence and reasoning, and assist agents with AI-generated reply suggestions.

---

## 🚀 Features

### 👤 User Features

- User registration with email verification
- User login using email and password
- Google OAuth2 login
- JWT-based authentication
- Raise support tickets
- View ticket details and status
- Track ticket status history
- Chat regarding tickets
- Change password
- Receive AI-generated response suggestions

### 🤖 AI-Powered Ticket Triage

TriageIQ uses Groq's LLM through Spring AI to analyze incoming tickets.

The AI can determine:

- Ticket department
- Ticket priority
- Confidence score
- Reasoning behind the classification
- Routing decision
- Suggested response

### 🎯 Ticket Classification

Tickets can be classified into priorities such as:

- LOW
- MEDIUM
- HIGH
- SEVERE

Tickets can also be routed to appropriate departments based on their content.

### 👨‍💻 Agent Features

- View assigned tickets
- View AI classification results
- Review AI-generated reply suggestions
- Edit AI-generated replies
- Update ticket status
- Communicate with customers
- Resolve tickets

### 👑 Admin Features

- Manage users
- Create staff accounts
- Manage departments
- Manage ticket categories
- Assign departments
- Manage system-level configurations

### 💬 Real-Time Chat

TriageIQ uses WebSocket and STOMP for real-time ticket communication between customers and support agents.

### 🔐 Security

- Spring Security
- JWT authentication
- Google OAuth2
- Role-based authorization
- Password hashing
- Authentication filters
- Protected REST APIs
- WebSocket authentication

### 📧 Email

The system supports email functionality for:

- Account activation
- Email verification
- Authentication-related communication

### 🐳 Docker Support

The application can be run using Docker Compose with:

- Spring Boot application
- MySQL 8
- Health checks
- Environment-based configuration

---

# 🏗️ Tech Stack

## Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Spring Security
- Spring AI
- Spring Validation
- Spring WebSocket
- Spring Mail

## AI

- Groq API
- Spring AI
- OpenAI-compatible API integration

## Database

- MySQL 8
- JPA / Hibernate

## Authentication

- JWT
- Google OAuth2
- Spring Security

## Build & Deployment

- Maven
- Docker
- Docker Compose

## Development Tools

- IntelliJ IDEA
- Postman
- Git
- GitHub

---

# 🧩 System Architecture

```text
                    ┌──────────────────────┐
                    │       Client         │
                    │   Web Application    │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │    Spring Boot API   │
                    └──────────┬───────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
              ▼                ▼                ▼
       ┌────────────┐   ┌──────────────┐  ┌──────────────┐
       │ Security   │   │ Ticket       │  │ WebSocket    │
       │ JWT/OAuth2 │   │ Management   │  │ Chat         │
       └────────────┘   └──────┬───────┘  └──────────────┘
                               │
                               ▼
                     ┌──────────────────┐
                     │ AI Classification│
                     │   Spring AI      │
                     └────────┬─────────┘
                              │
                              ▼
                       ┌─────────────┐
                       │  Groq LLM   │
                       └─────────────┘

                              │
                              ▼
                     ┌─────────────────┐
                     │      MySQL      │
                     └─────────────────┘


🤖 AI Ticket Processing Flow

When a customer creates a ticket:

Customer creates ticket
          │
          ▼
   Ticket is stored
          │
          ▼
   AI analyzes ticket
          │
          ▼
 ┌─────────────────────┐
 │ Department          │
 │ Priority            │
 │ Confidence          │
 │ Explanation         │
 │ Routing Decision    │
 └──────────┬──────────┘
            │
            ▼
 Ticket routed to
 appropriate department
            │
            ▼
 AI generates
 reply suggestion
            │
            ▼
 Agent reviews/edits
 the suggested reply
            │
            ▼
       Response sent

📁 Project Structure

src/main/java/com/example/TriageIQ/
├── Config/        # Security, CORS, WebSocket, Async, AI client config
├── Controller/     # REST + WebSocket message-mapped controllers
├── Service/        # Business logic — interface + Impl per feature
│   └── Ai/          # AI orchestration, prompt templates, response parsing
├── Repository/      # Spring Data JPA repositories
├── Entity/          # JPA entities + enums
├── DTO/             # Request/response DTOs
├── Security/         # JWT, OAuth2, current-user resolution, rate limiting
├── Exception/         # Custom exceptions + global exception handler
└── Validation/        # Ticket state machine, AI response validation

🔮 Future Enhancements

Possible future improvements include:

Advanced analytics dashboard
More AI providers
Improved AI classification accuracy
Ticket analytics and reporting
Notification system
Additional OAuth providers
Production cloud deployment
Automated testing and CI/CD


👩‍💻 Author

Alisha Shaikh
