# Resource Booking API

A secure RESTful Resource Booking System built with Spring Boot, Java 17, Spring Security, JWT authentication, JPA/Hibernate, and PostgreSQL.

## Features

- JWT-based authentication
- BCrypt password encryption
- ADMIN and USER role-based access control
- Resource CRUD operations
- Reservation CRUD operations
- Reservation ownership enforcement
- Reservation statuses: PENDING, CONFIRMED, CANCELLED
- Reservation filtering by status, minimum price, and maximum price
- Pagination using page and size
- Optional sorting
- Input validation
- Global exception handling
- PostgreSQL database integration
- Swagger/OpenAPI documentation
- Seed users for ADMIN and USER roles
- Unit and integration testing

## Technology Stack

- Java 17
- Spring Boot 3.3.5
- Spring Security
- JWT
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven
- Swagger / OpenAPI
- JUnit
- Spring Security Test

## Prerequisites

- Java 17+
- PostgreSQL
- Maven (optional because Maven Wrapper is included)
- Git

## Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE resource_booking;

## Environment Variables

Configure the following environment variables:

```text
DB_URL=jdbc:postgresql://localhost:5432/resource_booking
DB_USERNAME=postgres
DB_PASSWORD=your_database_password
JWT_SECRET=your_long_random_secret_key
JWT_EXPIRATION_MS=86400000

## Running the Application

### Windows

Run:

```bash
mvnw.cmd spring-boot:run


Then add:

```markdown
## Swagger / OpenAPI

Swagger UI:

`http://localhost:8080/swagger-ui/index.html`

OpenAPI specification:

`http://localhost:8080/v3/api-docs`


## Seed Users

The application automatically creates the following users for testing:

| Username | Password | Role |
|---|---|---|
| `admin` | `Admin@123` | ADMIN |
| `user`  | `User@123`  | USER |
| `user2` | `User2@123` | USER |

These credentials are intended for local testing only.