# Licence Plate Checker

A full-stack web application for validating and checking German licence plates (Kfz-Kennzeichen).

## Overview

This application consists of:
- **Backend**: Spring Boot application that provides REST APIs for licence plate validation
- **Frontend**: Angular application with a user interface for checking licence plates

## Prerequisites

- Java 17 or higher
- Node.js and npm
- Gradle (or use the included Gradle wrapper)
- MySQL8 (or change to H2 db in application.properties)

## Running the Application

### 1. Start the Backend (Spring Boot)

Use the Spring Boot run configuration in your IDE to start the backend server.

Alternatively, you can run it from the command line:

```bash
./gradlew bootRun
```


The backend will start at  http://localhost:8085.

### 2. Start the Frontend (Angular)

Navigate to the Angular application directory and use the start script:

```shell script
cd angular/licence-plate-app
npm run start
```


The Angular development server will start and the application will be available at http://localhost:4220.

## REST API Usage

The application provides a REST API for validating licence plates.

### Validate Licence Plate

**Endpoint:** `POST /licence-plate/validate`

**Request:**
```json
{
  "licencePlate": "B-AB-1234"
}
```


**Response (Success):**
```json
{
  "success": true,
  "result": "B-AB1234"
}
```


**Response (Error):**
```json
{
  "success": false,
  "error": "Invalid licence plate format"
}
```


**Example using curl:**
```shell script
curl -X POST http://localhost:8080/licence-plate/validate \
  -H "Content-Type: application/json" \
  -d '{"licencePlate": "B-AB-1234"}'
```


## Features

- Validate German licence plates
- Check licence plate format and structure
- Lookup distinguisher codes (Unterscheidungszeichen)
- REST API for integration with other applications

## Project Structure

```
licence-plate-checker/
├── src/                          # Spring Boot backend source code
│   ├── main/java/                # Java source files
│   └── main/resources/           # Application properties and data files
├── angular/licence-plate-app/    # Angular frontend application
│   ├── src/                      # Angular source files
│   └── package.json              # NPM dependencies and scripts
└── build.gradle.kts              # Gradle build configuration
```


## Development

The Angular application is configured with a proxy to forward API requests to the Spring Boot backend, allowing seamless development with both servers running locally.
