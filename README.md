# ECE318 – Library Management System

## Overview
This repository contains an individual coursework project for **ECE318: Programming Principles for Engineers**.  
The project implements a **Library / Bookstore Management System** that allows an administrator to manage books, main genres, and sub-genres through a graphical user interface, using **Java, SQL, and JDBC**.

The project is structured and documented to serve both as an **academic submission** and as a **portfolio artifact** demonstrating object-oriented design, GUI development, and database-backed applications.

---

## Key Characteristics
- Language: Java
- GUI: JavaFX 
- Persistence: SQL database accessed via JDBC
- Architecture: Layered design (GUI → Logic → Data Access)
- Build Tool: Maven
- Design Methodology: Object-Oriented Programming with UML documentation

---

## Functional Scope

### Books Management
- Display complete book information
- Create, read, update, and delete book records
- Search books by title or author
- Filter books by main genre and sub-genre (multiple selection supported)
- Sort books by price or rating (ascending / descending)
- Export book lists (full or filtered) to PDF reports

### Genres & Sub-Genres Management
- CRUD operations for main genres and sub-genres
- Keyword-based search
- Display hierarchical relationships between genres and sub-genres
- Compute and display:
  - Total number of books
  - Average rating
  - Average price
- Export analytical reports to PDF

---

## Dataset
The application logic is based on the **Amazon Books Dataset**, conceptually organized into three entities:

### Genre
- Main genre title
- Number of sub-genres
- Amazon URL

### Sub-Genre
- Sub-genre title
- Associated main genre
- Number of books
- Amazon URL

### Book
- Title
- Author
- Main genre
- Sub-genre
- Type (Paperback, Kindle, Hardcover, Audiobook, etc.)
- Price
- Rating
- Number of ratings
- Amazon URL

CSV files included in the repository are provided for reference and testing.  
The running application operates on an **SQL database**, not directly on CSV files.

---

## Database Requirements (Mandatory)

### General Behavior
- The application **does not start, create, or migrate the database automatically**
- A compatible **SQL database server must already be running**
- The database schema must be **created manually before execution**
- Database connection parameters are defined in the source code

### User Responsibilities
Before running the application, the user must:
1. Start the SQL database server
2. Create the required database
3. Create all necessary tables according to the application schema
4. Ensure database credentials and JDBC configuration match those defined in the code

If any of the above conditions are not met, the application will fail at runtime.

---

## Object-Oriented Design

### UML Documentation
The repository includes:
- Class Diagram
- Use Case Diagram

These diagrams describe:
- Class responsibilities and relationships
- Associations, dependencies, and inheritance
- Administrator interactions with the system

### OOP Principles Demonstrated
- **Encapsulation**: Controlled access to internal data through class interfaces
- **Abstraction**: Separation of GUI, business logic, and persistence layers
- **Inheritance**: Shared behavior across related domain entities
- **Polymorphism**: Flexible method implementations across class hierarchies

---

## Technology Stack
- Java
- Maven
- JavaFX
- SQL (via JDBC)
- draw.io (UML diagrams)

---

## Repository Structure
```
ECE318_LibraryManagementSys/
│ ├── src/ # Application source code
│ │ ├── main/java/
│ │ │ ├── model/ # Entity classes
│ │ │ ├── dao/ # Data access logic (SQL/JDBC)
│ │ │ ├── controller/ # GUI controllers
│ │ │ └── Main.java # Application entry point
│ ├── pom.xml # Maven configuration
│ ├── mvnw / mvnw.cmd # Maven wrapper
│ └── target/ # Build output
│
├── Books_df.csv # Reference dataset
├── Genre_df.csv
├── Sub_Genre_df.csv
├── class_diag318.drawio.png # Class diagram
├── use_case318.drawio.png # Use case diagram
└── README.md
```
---

## Build and Run

1. Ensure Java, Maven, and the SQL database server are installed
2. Initialize the database schema as required by the application
3. Verify database connection settings in the source code
4. Navigate to the project directory:
```
cd ECE318_LibraryManagementSys_UC1069790
```
5. Build the project:
```
mvn clean install
```
6. Run the application using the configured main class

---

## Academic Context
- Course: **ECE318 – Programming Principles for Engineers**
- Semester: Fall 2025
- Submission Type: Individual coursework
- Evaluation Focus:
- Object-Oriented Programming principles
- UML design and explanation
- GUI functionality
- SQL database integration
- Code clarity and explainability

---

## Notes
- Database initialization is a prerequisite
- No automatic schema creation or migration is implemented
- The project is designed for live demonstration and oral explanation
- Code structure prioritizes clarity, modularity, and maintainability
