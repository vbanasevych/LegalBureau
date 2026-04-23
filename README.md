# ⚖️ LegalBureau — CRM System for Law Firms

**LegalBureau** is a multi-user web application designed to automate the internal processes of a law firm. The system provides a complete lifecycle for managing legal cases: from client registration and scheduling court hearings to automatic invoice generation and data analytics.

This project was developed as part of the laboratory work for the "Instrumental Environments and Programming Technologies" course.

---

## 🚀 Key Features

* **Role-Based Access Control (RBAC):** Secure access distribution across three user levels: `ADMIN`, `LAWYER`, and `CLIENT`.
* **Case Management:** Create cases, log provided legal services, and schedule court hearings or client consultations.
* **Data Ports (Import/Export):**
    * Mass "partial" import of cases from `.xlsx` and `.docx` files with transactional protection against duplicates and invalid data.
    * Export official case dossiers in `.docx` format for printing and signing.
    * Export case registries to Excel (`.xlsx`) for reporting.
* **Financial Accounting:** Automatic invoice generation based on the total cost of services provided by the lawyer.
* **Interactive Analytics:** Admin dashboards featuring visual statistics on case success rates (integrated with Google Charts API).
* **Security:** Secure password hashing (BCrypt), email-based account verification, and secure password recovery via unique tokens.

---

## 🛠 Tech Stack

| Component | Technologies |
| :--- | :--- |
| **Back-end** | Java 21, Spring Boot 3 (Web, Data JPA, Security) |
| **Database** | PostgreSQL, Liquibase (8 stages of schema migrations) |
| **Document Processing**| Apache POI (Word, Excel) |
| **Front-end** | Thymeleaf, Bootstrap 5, Google Charts API |
| **Infrastructure** | Docker, Docker Compose, Maven |

---

## ⚙️ Running the Project

### 🐳 Option 1: Via Docker (Recommended)

To quickly spin up the entire infrastructure (application + database):

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/vbanasevych/LegalBureau.git](https://github.com/vbanasevych/LegalBureau.git)
    cd LegalBureau
    ```
2.  **Start the containers:**
    ```bash
    docker-compose up --build -d
    ```
3.  The application will be available at: `http://localhost:8085`

### 🏗 Option 2: Local Run (Maven)

1.  Configure your local PostgreSQL connection inside `src/main/resources/application.properties`.
2.  Build and run the application:
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```
*(Note: Liquibase will automatically create all necessary tables and populate the database with initial test data upon the first run).*

---

## 👤 Test Credentials

You can use the following pre-configured accounts to explore the system's functionality (the password for all test clients is `password098`, for all test lawyers and admin is `password123`):

* **Admin:** `viktoriabanasevic@gmail.com`
* **Lawyer:** `kovalenko@bureau.com`
* **Client:** `client1@gmail.com`

---

## 📂 Project Structure

* `src/main/java/com/legalbureau` — Core business logic (Controllers, Services, Entities).
* `src/main/resources/db/changelog` — SQL migrations managed by Liquibase.
* `src/main/resources/templates` — Server-side HTML templates (Thymeleaf).
* `docker-compose.yml` — Configuration for containerized deployment.

---
**Developer:** [Viktoriia Banasevych](https://github.com/vbanasevych)
