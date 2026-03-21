# Student Management System — Microservices Architecture

> SLIIT | SE4010 Cloud Computing Assignment | Spring Boot + Kafka + Docker + Azure

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Student Management System                         │
│                                                                     │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐          │
│  │ auth-service │    │ user-service │    │course-service│          │
│  │  port: 8081  │    │  port: 8082  │    │  port: 8083  │          │
│  │              │    │              │    │              │          │
│  │ - JWT Auth   │    │ - Profiles   │    │ - Courses    │          │
│  │ - Register   │    │ - Students   │    │ - Enrollment │          │
│  │ - Login      │    │ - Teachers   │    │ - CRUD       │          │
│  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘          │
│         │                   │                   │                  │
│         └──────────────┬────┘                   │                  │
│                        │         ┌──────────────┘                  │
│              ┌─────────▼─────────▼──────┐                         │
│              │       Apache Kafka        │                         │
│              │                          │                         │
│              │  Topics:                 │                         │
│              │  - user.registered       │                         │
│              │  - enrollment.created    │                         │
│              │  - marks.updated         │                         │
│              └──────────────┬───────────┘                         │
│                             │                                     │
│                    ┌────────▼─────────┐                           │
│                    │  marks-service   │                           │
│                    │   port: 8084     │                           │
│                    │                 │                           │
│                    │ - Modules       │                           │
│                    │ - Marks/Grades  │                           │
│                    │ - GPA Reports   │                           │
│                    └─────────────────┘                           │
└─────────────────────────────────────────────────────────────────────┘
```

## Kafka Communication Flow

| Topic | Producer | Consumer | Trigger |
|---|---|---|---|
| `user.registered` | auth-service | user-service | User registration creates profile |
| `enrollment.created` | course-service | marks-service | Enrollment logged for marks tracking |
| `marks.updated` | marks-service | user-service | GPA recalculated and synced to profile |

## Services

### 1. Auth Service (port 8081)
**Responsibility:** JWT-based authentication and authorization

| Endpoint | Method | Description |
|---|---|---|
| `/api/auth/register` | POST | Register new user (STUDENT/TEACHER/ADMIN) |
| `/api/auth/login` | POST | Login and receive JWT token |
| `/api/auth/validate` | GET | Validate JWT token |
| `/swagger-ui.html` | GET | API documentation |

**Kafka:** Publishes `user.registered` event on every registration

### 2. User Service (port 8082)
**Responsibility:** User profile management for students and teachers

| Endpoint | Method | Description |
|---|---|---|
| `/api/users` | GET | Get all users |
| `/api/users/{userId}` | GET | Get user profile |
| `/api/users/username/{username}` | GET | Get by username |
| `/api/users/students` | GET | Get all students |
| `/api/users/teachers` | GET | Get all teachers |
| `/api/users/{userId}` | PUT | Update profile |
| `/api/users/{userId}` | DELETE | Delete profile |

**Kafka:** Consumes `user.registered` (auto-creates profile), consumes `marks.updated` (updates GPA)

### 3. Course Service (port 8083)
**Responsibility:** Course management and student enrollment

| Endpoint | Method | Description |
|---|---|---|
| `/api/courses` | GET/POST | List/Create courses |
| `/api/courses/{id}` | GET/PUT/DELETE | Course CRUD |
| `/api/courses/code/{code}` | GET | Find by course code |
| `/api/courses/{id}/enroll` | POST | Enroll student |
| `/api/courses/{id}/enrollments` | GET | Course enrollments |
| `/api/courses/enrollments/student/{id}` | GET | Student enrollments |

**Kafka:** Publishes `enrollment.created` on student enrollment

### 4. Marks Service (port 8084)
**Responsibility:** Module management and student marks/grade tracking

| Endpoint | Method | Description |
|---|---|---|
| `/api/modules` | GET/POST | List/Create modules |
| `/api/modules/{id}` | GET/PUT/DELETE | Module CRUD |
| `/api/modules/course/{courseId}` | GET | Modules by course |
| `/api/marks` | POST | Add student mark |
| `/api/marks/student/{id}` | GET | All marks for student |
| `/api/marks/student/{id}/course/{cid}` | GET | Marks by student + course |
| `/api/marks/{id}` | PUT | Update mark |
| `/api/marks/report/student/{id}/course/{cid}` | GET | Grade report + GPA update |

**Kafka:** Consumes `enrollment.created`, publishes `marks.updated` (GPA events)

## Quick Start

### Prerequisites
- Docker and Docker Compose
- Java 17+ (for local development)
- Maven 3.9+

### Run with Docker Compose

```bash
# Start all services (PostgreSQL x4, Zookeeper, Kafka, 4 Spring Boot apps)
docker-compose up -d

# View logs
docker-compose logs -f auth-service

# Stop everything
docker-compose down -v
```

Services will be available at:
- Auth Service: http://localhost:8081/swagger-ui.html
- User Service: http://localhost:8082/swagger-ui.html
- Course Service: http://localhost:8083/swagger-ui.html
- Marks Service: http://localhost:8084/swagger-ui.html

### Run Individual Service Locally

```bash
# Start infrastructure first
docker-compose up -d postgres-auth zookeeper kafka

cd auth-service
mvn spring-boot:run
```

## Docker Build

```bash
# Build individual service
docker build -t sms-auth-service:latest ./auth-service

# Build all via compose
docker-compose build
```

## Azure Container Apps Deployment

```bash
az login
chmod +x azure/deploy.sh
./azure/deploy.sh
```

See [azure/azure-container-apps.yml](azure/azure-container-apps.yml) for full configuration.

## CI/CD Pipeline (GitHub Actions)

| Workflow | Trigger | Actions |
|---|---|---|
| `auth-service-ci.yml` | Push to `auth-service/**` | Build, Test, SonarCloud, Push to GHCR |
| `user-service-ci.yml` | Push to `user-service/**` | Build, Test, SonarCloud, Push to GHCR |
| `course-service-ci.yml` | Push to `course-service/**` | Build, Test, SonarCloud, Push to GHCR |
| `marks-service-ci.yml` | Push to `marks-service/**` | Build, Test, SonarCloud, Push to GHCR |
| `security-scan.yml` | Push to `main` / weekly | Snyk dependency vulnerability scan |

### Required GitHub Secrets

```
SONAR_TOKEN       - SonarCloud project token
SNYK_TOKEN        - Snyk API token
AZURE_CREDENTIALS - Azure service principal JSON
```

## Security

- JWT Bearer token authentication on all services
- BCrypt password hashing (no plaintext passwords stored)
- Stateless session management
- Environment variable based secrets (no hardcoded credentials)
- SonarCloud SAST scanning in CI pipeline
- Snyk dependency vulnerability scanning
- Each service has its own isolated PostgreSQL database
- Docker bridge network for service isolation

## Tech Stack

| Component | Technology |
|---|---|
| Framework | Spring Boot 3.2.3 |
| Language | Java 17 |
| Security | Spring Security + JJWT 0.11.5 |
| Database | PostgreSQL 15 (one per service) |
| ORM | Spring Data JPA / Hibernate |
| Messaging | Apache Kafka (Confluent 7.5.0) |
| API Docs | SpringDoc OpenAPI / Swagger UI |
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| Container Registry | GitHub Container Registry (GHCR) |
| Cloud | Azure Container Apps |
| SAST | SonarCloud |
| Dependency Scan | Snyk |

## Project Structure

```
microservice-student-management-system/
├── auth-service/           # Authentication & JWT
├── user-service/           # User Profile Management
├── course-service/         # Course & Enrollment
├── marks-service/          # Modules & Grades
├── azure/
│   ├── deploy.sh
│   └── azure-container-apps.yml
├── .github/workflows/      # CI/CD pipelines
└── docker-compose.yml      # Full local stack
```
