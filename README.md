# School Management System

A comprehensive School Management System built with Java EE, following the MVC architecture.

## Features

- **Authentication System**
  - User login based on UserType (Admin, Teacher, Student, Parent, Staff)
  - Role-based access control for different system areas

- **User Management**
  - Create, read, update, and delete users
  - Different user types with specific permissions
  - Profile image management

- **Academic Management**
  - Teacher management
  - Student management
  - Course management
  - Grading system

- **Parent Communication**
  - View student information
  - Schedule appointments

- **Announcement System**
  - Create and manage announcements for different target groups

- **Reporting**
  - Generate reports based on various criteria
  - Export reports to PDF and CSV formats

- **Health Management**
  - Track student health records
  - Manage nurse and doctor information
  - Record diagnoses and results

## Technology Stack

- **Backend**: Java EE (Servlets, JSP)
- **Frontend**: HTML, CSS, JavaScript, Bootstrap 4
- **Database**: MySQL
- **ORM**: JDBC (Manual DAO implementation)
- **Others**:
  - BCrypt for password hashing
  - iText for PDF generation
  - Apache POI for Excel generation
  - File upload management

## Project Structure

The project follows the MVC (Model-View-Controller) architecture:

- **Model**: Java classes for each entity
- **DAO**: Data access objects for database operations
- **Controller**: Servlets to handle requests
- **Views**: JSP pages
- **Utilities**: Helper classes

## Database Schema

The system uses MySQL database with the following main tables:
- Users
- Teachers
- Students
- Parents
- Courses
- Marks
- Announcement
- StudentBehavior
- StudentTracking
- Reports
- Appointment
- Nurses
- Doctors
- Diagnosis

## Getting Started

### Prerequisites

- JDK 11 or higher
- Apache Tomcat 9 or higher
- MySQL 8.0 or higher
- Maven

### Setup and Installation

1. Clone the repository
2. Import the project into your IDE as a Maven project
3. Create MySQL database and run the SQL script in `sms_database.sql`
4. Update database connection details in `src/main/java/com/sms/util/DBConnection.java`
5. Build the project using Maven: `mvn clean package`
6. Deploy the generated WAR file to Tomcat

### Default Login Credentials

- **Username**: admin
- **Password**: admin123


## License

This project is licensed under the MIT License - see the LICENSE file for details.
