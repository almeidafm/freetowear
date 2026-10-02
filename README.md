<div align="center">

# FreeToWear

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring%20Boot](https://img.shields.io/badge/Spring_Boot-3.5.7-6DB33F?style=flat&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat&logo=mysql&logoColor=white)

> FreeToWear is an e-commerce platform for a custom-shirt business, designed to replace a manual sales process
based on in-person and WhatsApp orders. Built with Spring Boot.

</div>

## Table of Contents

- [About](#about)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Security](#security)
- [Contributing](#contributing)
- [Roadmap](#roadmap)
- [License](#license)

## About

FreeToWear provides an online storefront for customers to discover products, manage their accounts, and place orders. Store administrators can manage products, categories, product variations, coupons, and order tracking. The project aims to reduce friction in the buying process, make purchases faster, and avoid losing sales while customers wait for a response.

## Tech Stack

- Java 21
- Spring Boot 3.5.7
- Spring MVC and Thymeleaf
- Spring Data JPA with Hibernate
- Spring Security
- MySQL
- Cloudinary
- Maven


## Features

### Customer experience

- Account registration, login, profile updates, email changes, and account deactivation
- Email verification and password recovery by email
- Multiple delivery addresses
- Product browsing by category, product search, and product details
- Product ratings and reviews, including review images
- Cart management, quantity updates, checkout, and order history
- Order cancellation, payment-method selection, pending payment records, and order tracking
- Discount coupon validation
- Image uploads for product and order-related content

### Administration

- Product and product-variation management
- Category management
- Coupon creation and updates
- Order-tracking updates
- Product image storage through Cloudinary

## Architecture

FreeToWear is a single-module Spring Boot application:

1. Thymeleaf templates and static CSS/JavaScript provide the web storefront.
2. Web controllers handle browser routes and render Thymeleaf views.
3. API controllers expose endpoints for customer and administrator data operations.
4. Services contain account, catalog, coupon, order, review, and verification business logic.
5. Spring Data repositories persist customers, products, orders, reviews, payments, and related entities in MySQL.
6. Spring Security protects authenticated resources and administrator operations.
7. Cloudinary stores product and user-uploaded images.
8. SMTP is used for email verification and password-reset messages.

## Project Structure

```text
src/
├── main/
│   ├── java/com/freetowear/
│   │   ├── config/          Security and Cloudinary configuration
│   │   ├── controller/      Web controllers and customer/admin API controllers
│   │   ├── dto/             Request and response DTOs
│   │   ├── entity/          JPA domain entities
│   │   ├── enums/           Domain enumerations
│   │   ├── infra/           Cloudinary, email, and security integrations
│   │   ├── repository/      Spring Data JPA repositories
│   │   ├── service/         Application and business logic
│   │   └── util/            Shared utilities and entity listeners
│   └── resources/
│       ├── templates/       Thymeleaf pages
│       ├── static/          CSS, JavaScript, and image assets
│       └── application.properties
└── test/
    └── java/com/freetowear/ Automated tests
```

## Getting Started

### Prerequisites

- Java 21
- Maven 3.9 or newer
- MySQL
- A Cloudinary account for image features
- An SMTP account for email verification and password recovery

### Clone the repository

```bash
git clone https://github.com/almeidafm/freetowear.git
cd freetowear
```

### Configure environment variables

The application reads the following environment variables:

| Variable | Purpose |
| --- | --- |
| `DB_URL` | MySQL JDBC connection URL |
| `DB_USERNAME` | MySQL username |
| `DB_PASSWORD` | MySQL password |
| `CLOUDINARY_URL` | Cloudinary connection URL |
| `SPRING_MAIL_USERNAME` | SMTP username |
| `SPRING_MAIL_PASSWORD` | SMTP password |
| `REMEMBER_ME_KEY` | Secret key used to sign remember-me authentication tokens |

The configured SMTP host is `mx.msgwing.com` on port `587` with authentication and STARTTLS enabled.

### Create a `.env` file in the project root

Example `.env` file:

```dotenv
DB_URL='jdbc:mysql://localhost:3306/freetowear'
DB_USERNAME='your_mysql_user'
DB_PASSWORD='your_mysql_password'
REMEMBER_ME_KEY='secret-key'
CLOUDINARY_URL='cloudinary://your_api_key:your_api_secret@your_cloud_name'
SPRING_MAIL_USERNAME='your_smtp_username'
SPRING_MAIL_PASSWORD='your_smtp_password'
```

Do not commit passwords, API credentials, remember-me keys, or local `.env` files.

### Database

Create the MySQL database before starting the application, for example:

```sql
CREATE DATABASE freetowear CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Grant the configured database user access to this database. Hibernate updates the application schema automatically (`ddl-auto=update`), and Spring Session is configured to initialize its JDBC schema. This is convenient for development. For production, use a migration tool such as Flyway or Liquibase and review the session-schema initialization strategy.

The application does not seed an administrator account. Create a regular account through the registration flow, then assign the `ROLE_ADMIN` role through a controlled database operation or an administrative provisioning process before using the administrator endpoints. Do not expose that operation publicly.

```sql
UPDATE customer SET role = 'ROLE_ADMIN' WHERE email = 'you@example.com';
```

### Run the project

Before you start, make sure that:

- MySQL is running and the database exists (see [Database](#database)).
- The `.env` file is in the project root.

Run all commands from the project root, the folder that contains `pom.xml`.

#### Option 1: Run with Maven

macOS/Linux:

```bash
./mvnw spring-boot:run
```

Windows (PowerShell or Command Prompt):

```powershell
.\mvnw.cmd spring-boot:run
```

The first run downloads the dependencies, so it can take a few minutes. When the log shows a line like `Started ... in ... seconds`, open <http://localhost:8080> in your browser. Press `Ctrl+C` to stop the application.

If Maven is already installed, you can use `mvn spring-boot:run` instead.

#### Option 2: Build and run the JAR

Build the project.

macOS/Linux:

```bash
./mvnw clean package
```

Windows (PowerShell or Command Prompt):

```powershell
.\mvnw.cmd clean package
```

Then start the application. This command is the same on every system:

```bash
java -jar target/FreeToWear-0.0.1-SNAPSHOT.jar
```

The JAR file name depends on the version in `pom.xml`.

To use the administrator pages, register an account and give it the `ADMIN` role first (see [Database](#database)).

## Security

- Passwords are encoded with BCrypt.
- Public and authenticated routes are explicitly configured in Spring Security.
- Administrator-only operations protect product, category, coupon, and tracking management.
- Sessions use JDBC storage, have a 30-minute timeout, and allow one active session per customer; logging in again expires the previous session.
- Remember-me authentication uses a JDBC token repository.
- Uploaded images are limited to JPEG, PNG, and WebP MIME types and 5 MB per file.

Before deploying, keep database, SMTP, Cloudinary credentials, and the remember-me key outside version control. Review database schema management, upload validation, session configuration, CSRF and authorization behavior, and production error handling for the target environment.

## Contributing

This is a portfolio project and is not currently open to contributions. Feedback and suggestions are welcome via issues.

## Roadmap

- Improve centralized error handling and automated test coverage
- Add rate limiting and abuse protection
- Expand authorization checks and auditing for critical actions
- Add application event logging and monitoring
- Integrate payment processing with an external payment provider
- Add production deployment and operational configuration
- Continue improving performance, security, and maintainability

## License

This project is licensed under the [MIT License](LICENSE).
