# JPA Criteria API Example

This project demonstrates comprehensive usage patterns for JPA Criteria API and Hibernate extensions through practical examples.

## Overview

The JPA Criteria API Example project provides extensive code samples showing how to use JPA Criteria API effectively, including standard JPA features and Hibernate-specific extensions. This project serves as a comprehensive reference for developers working with dynamic queries in Java applications.

## Features

### JPA Criteria API Examples
- **Basic Usage**: Entity retrieval, Tuple queries, fetch/join operations, cursor processing
- **SELECT Clause**: Column selection, constants, calculations, functions, CASE expressions, aggregations, scalar subqueries
- **FROM Clause**: Single/multiple table queries, inner/outer joins, fetch strategies, relationship handling
- **WHERE Clause**: Simple/complex conditions, LIKE/IN/NULL/BETWEEN predicates, EXISTS/NOT EXISTS subqueries
- **Other Clauses**: GROUP BY, HAVING, ORDER BY, SELECT FOR UPDATE (pessimistic locking)

### Hibernate Extensions
- **WITH Clause (CTE)**: Common Table Expressions with WITH RECURSIVE for sequence generation
- **FROM Clause Extensions**: JOIN operations between unrelated entities, derived tables with subqueries
- **Extended Functions**: Advanced mathematical, string, and datetime functions beyond JPA standard

## Technology Stack

- **Java**: 21
- **Spring Boot**: 3.5.4
- **Hibernate**: 6.6.22.Final (via Spring Boot)
- **Database**: H2 (default), MySQL (optional)
- **Build Tool**: Gradle 8.14.3
- **Lombok**: 1.18.38

## Project Structure

```
src/
├── entity/java/com/example/db/entity/     # JPA Entities (separate source set)
├── main/java/com/example/
│   ├── jpa/                              # Standard JPA Criteria examples
│   │   ├── JpaBasicUsageExample.java
│   │   ├── JpaSelectClauseExample.java
│   │   ├── JpaFromClauseExample.java
│   │   ├── JpaWhereClauseExample.java
│   │   └── JpaOtherUsageExample.java
│   ├── hibernate/                        # Hibernate-specific extensions
│   │   ├── HibernateWithClauseExample.java
│   │   ├── HibernateFromClauseExample.java
│   │   └── HibernateFunctionExample.java
│   ├── JpaConfiguration.java             # JPA configuration
│   ├── Main.java                         # Application entry point
│   ├── PrepareExample.java               # Sample data setup
│   └── Runner.java                       # Example execution coordinator
└── main/resources/
    ├── application.properties            # Default configuration (H2)
    └── application-mysql.properties      # MySQL configuration
```

## Getting Started

### Prerequisites

- Java 21 or higher
- Docker (optional, for MySQL)

### Running with H2 (Default)

```bash
# Build the project
./gradlew build

# Run with H2 in-memory database
./gradlew bootRun
```

### Running with MySQL

1. Start MySQL using Docker Compose:
```bash
docker-compose up -d mysql
```

2. Run the application with MySQL profile:
```bash
./gradlew bootRun --args='--spring.profiles.active=mysql'
```

## Architecture Highlights

### Multi-Source Set Configuration

The project uses a unique Gradle source set configuration to handle JPA metamodel generation:

- **`src/entity/`**: Contains JPA entities in a separate source set
- **`src/main/`**: Main application code that uses the entities  
- **Generated metamodels**: Automatically created in `build/generated/sources/annotationProcessor/java/entity`

This separation prevents compilation issues where metamodel generation requires entities to compile first, avoiding circular dependency problems.

#### Gradle Configuration Details

The build.gradle includes three source sets:

```gradle
sourceSets {
    entity                    // JPA entities
    entitymodel {             // Generated JPA metamodels (for IDE support)
        java {
            srcDir file('build/generated/sources/annotationProcessor/java/entity')
        }
    }
}
```

Key points:
- **Entity compilation**: Entities must be compiled first to generate metamodels via `hibernate-jpamodelgen`
- **IDE compatibility**: The `entitymodel` source set ensures IntelliJ IDEA recognizes generated metamodel classes
- **Build process**: While Gradle can build successfully without the `entitymodel` source set, it's essential for IDE error-free development
- **Metamodel generation**: Annotation processor automatically generates metamodel classes (e.g., `Customer_.java`) during entity compilation

### Example Execution Flow

1. **PrepareExample**: Sets up sample data (Customer, Product, SalesOrder, etc.)
2. **JPA Examples**: Demonstrates standard JPA Criteria API patterns
3. **Hibernate Examples**: Shows Hibernate-specific extensions and advanced features

## Key Learning Points

### N+1 Problem Solutions
- Using `fetch()` for eager loading of related entities
- Comparing fetch vs join strategies for different use cases

### Complex Query Patterns
- Scalar subqueries for calculated fields
- EXISTS/NOT EXISTS for efficient existence checks
- Latest record extraction from history tables using NOT EXISTS

### Hibernate Advanced Features
- WITH RECURSIVE for sequence generation bypassing recursion limits
- Cross-entity JOINs without defined relationships using ON clause
- Derived tables with subqueries in FROM clause

## Development

### Build Commands
```bash
# Clean build
./gradlew clean build

# Run tests
./gradlew test

# Generate documentation
./gradlew javadoc
```

### Database Schema
The application uses auto-generated schema with the following entities:
- Customer (customers)
- Product (products) 
- SalesOrder (sales orders with status)
- SalesOrderItem (order line items)
- SalesOrderHistory (order change history)

## Configuration

### H2 Configuration
- In-memory database with console access
- Auto-DDL generation enabled
- SQL logging enabled for learning purposes

### MySQL Configuration  
- Connection via Docker Compose
- Database: `jpacriteria`
- User: `jpacriteria` / Password: `jpacriteria`

## License

Licensed under the Apache License, Version 2.0. See LICENSE file for details.

## Contributing

This project serves as an educational resource. Contributions that add new JPA/Hibernate patterns or improve existing examples are welcome.
