# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a JPA Criteria API example project demonstrating various JPA and Hibernate query patterns. The project uses Spring Boot 3.5.4 with Java 21 and includes examples for both standard JPA Criteria queries and Hibernate-specific extensions.

This project serves as a comprehensive educational resource with extensive JavaDoc documentation and detailed README files in both English and Japanese.

## Build System and Commands

This project uses Gradle with the wrapper included.

### Core Commands
- **Build the project**: `./gradlew build`
- **Run the application**: `./gradlew bootRun`
- **Run tests**: `./gradlew test`
- **Clean build**: `./gradlew clean build`
- **Generate JavaDoc**: `./gradlew javadoc`

### Database Options
- **Default (H2)**: Run with default profile for in-memory H2 database
- **MySQL**: Use `--spring.profiles.active=mysql` and start MySQL with `docker-compose up -d mysql`

## Architecture

### Multi-Source Set Structure

The project uses a unique Gradle source set configuration to handle JPA metamodel generation:

- **`src/entity/`**: Contains JPA entities (separate source set)
- **`src/main/`**: Main application code that uses the entities
- **Generated metamodels**: Automatically generated in `build/generated/sources/annotationProcessor/java/entity`

This separation prevents compilation issues where metamodel generation requires entities to compile first.

### Key Components

- **Entities**: Located in `src/entity/java/com/example/db/entity/` (Customer, Product, SalesOrder, etc.)
- **JPA Examples**: `src/main/java/com/example/jpa/` - Standard JPA Criteria API examples
- **Hibernate Examples**: `src/main/java/com/example/hibernate/` - Hibernate-specific query extensions
- **Runner**: `src/main/java/com/example/Runner.java` - Executes all examples in sequence

### Data Preparation

The `PrepareExample` class sets up sample data before running query examples.

### Configuration

- **JPA Config**: `JpaConfiguration.java` configures entity scanning and shared EntityManager
- **Database**: H2 by default, MySQL support via `application-mysql.properties`
- **Logging**: Configured to show SQL queries and binding parameters for learning purposes

## Development Notes

- Entity compilation must happen before main compilation due to metamodel generation
- The project demonstrates advanced JPA/Hibernate features like WITH clauses and custom functions
- All example classes implement a common pattern with an `execute()` method
- Lombok is used for entity boilerplate reduction
- Comprehensive JavaDoc comments are included for all classes and key methods
- README files are available in both English (README.md) and Japanese (README.ja.md)

## Documentation

- **README.md**: Comprehensive project documentation in English
- **README.ja.md**: Complete project documentation in Japanese
- **JavaDoc**: Extensive inline documentation for all classes and methods
- **CLAUDE.md**: Development guidance for Claude Code users