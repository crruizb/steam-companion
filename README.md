# Steam Companion

A full-stack application for managing and tracking your Steam game library, achievements, and statistics.

## 🏗️ Architecture

This project consists of three main components:

- **Backend**: Spring Boot 4 REST API built with Kotlin
- **Frontend**: React 19 with TypeScript, Vite, and TailwindCSS
- **CI/CD**: GitHub Actions for automated building, testing, and deployment

## 📋 Prerequisites

### Backend

- Java 25 (JDK)
- PostgreSQL 18
- Gradle (wrapper included)
- Steam API Key

### Frontend

- Node.js 18+
- pnpm (recommended) or npm

### Deployment

- Docker & Docker Compose
- DockerHub account (for CI/CD deployment)

## 🚀 Quick Start

### 1. Backend Setup

#### Using Docker Compose (Recommended)

```bash
cd backend

# Start PostgreSQL database
docker-compose up -d

# Set required environment variables
export STEAM_API_KEY=your_steam_api_key_here

# Run the application
./gradlew bootRun
```

The backend API will be available at `http://localhost:8080` |

### 2. Frontend Setup

```bash
cd frontend

# Install dependencies
pnpm install

# Start development server
pnpm dev
```

The frontend will be available at `http://localhost:5173`

#### Available Scripts

- `pnpm dev` - Start development server with hot reload
- `pnpm build` - Build for production
- `pnpm preview` - Preview production build locally
- `pnpm lint` - Run ESLint

## 🔄 CI/CD Pipeline

The project uses GitHub Actions for continuous integration and deployment. The workflow is defined in `.github/workflows/ci.yml`.

### Workflow Overview

The CI/CD pipeline consists of two jobs:

#### 1. Build Job

- Triggers on push to `main`, pull requests, or manual dispatch
- Sets up JDK 25 with Gradle caching
- Builds the backend with `./gradlew clean build`
- Uploads the JAR artifact for the deploy job

#### 2. Deploy Job

- Runs after successful build
- Downloads the JAR artifact
- Builds a Docker image with a timestamp tag
- Pushes to DockerHub with both timestamped and `latest` tags

### Required GitHub Secrets

Configure these secrets in your repository settings (Settings → Secrets → Actions):

| Secret               | Description             |
| -------------------- | ----------------------- |
| `DOCKERHUB_USERNAME` | Your DockerHub username |
| `DOCKERHUB_TOKEN`    | DockerHub access token  |

### Manual Deployment

You can trigger a deployment manually from the GitHub Actions tab using the "workflow_dispatch" event.

## 🧪 Testing

### Backend Tests

```bash
cd backend

# Run all tests
./gradlew test
```

## UI

![Steam companion UI](images/SteamCompanionUI.png "Steam companion UI")
