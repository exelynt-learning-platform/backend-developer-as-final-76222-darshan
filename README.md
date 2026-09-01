# Resource Booking API

A secure RESTful Resource Booking System built with Spring Boot, Java 17, Spring Security, JWT authentication, JPA/Hibernate, and MySQL/PostgreSQL.

## Features

- JWT-based authentication
- BCrypt password encryption
- ADMIN and USER role-based access control
- Resource management
- Reservation management
- Reservation ownership enforcement
- Reservation statuses:
  - PENDING
  - CONFIRMED
  - CANCELLED
- Reservation filtering by:
  - Status
  - Minimum price
  - Maximum price
- Pagination using `page` and `size`
- Optional sorting
- Input validation
- Global exception handling
- MySQL/PostgreSQL database support
- Swagger/OpenAPI documentation
- Seed users for ADMIN and USER roles
- Unit/integration testing

## Technology Stack

- Java 17
- Spring Boot 3.3.5
- Spring Security
- JWT
- Spring Data JPA / Hibernate
- MySQL / PostgreSQL
- Maven
- Swagger / OpenAPI
- JUnit
- Spring Security Test

## Project Structure

```text
src/main/java/com/example/booking
├── config
├── controller
├── dto
├── entity
├── exception
├── repository
├── security
└── service