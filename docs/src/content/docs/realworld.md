---
title: "RealWorld"
---

# RealWorld

Medium.com clone (Conduit) implementing the RealWorld specification with full CRUD operations, JWT authentication, article management, comments, favorites, tags, and user following.

## Overview

RealWorld is a Medium.com clone that adheres to the RealWorld specification, providing a standardized way to build and compare fullstack applications. It includes comprehensive features for article publishing, user interactions, and social features.

## Key Technologies

- **MicroProfile JWT** - Token-based authentication
- **JAX-RS** - RESTful API design
- **JPA with PostgreSQL** - Data persistence
- **BCrypt** - Password hashing
- **Testcontainers** - Integration testing
- **MicroShed Testing** - Testing framework

## Architecture Highlights

- Complete RESTful API implementation
- JWT-based authentication and authorization
- Article CRUD with slug generation
- Comment system on articles
- User favorites and following
- Tag-based article categorization
- Pagination and filtering
- Comprehensive exception handling
- Integration tests with real database (Testcontainers)
- Secure password storage with BCrypt
