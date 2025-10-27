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

## Running the Application

### 1. Start the Backend (Spring Boot)

Use the Spring Boot run configuration in your IDE to start the backend server.

Backend will be available on http://localhost:8085 
(configure in application.properties)

### 2. Start the Frontend (Angular)

Navigate to the Angular application directory and use the start script:

```bash
npm run start
```

Application will be available at http://localhost:4220.

### Features
- Validate German licence plates
- Check licence plate format and structure
- Lookup distinguisher codes (Unterscheidungszeichen)
- REST API for integration with other applications