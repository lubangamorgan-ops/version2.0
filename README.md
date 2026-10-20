# MIMS — Machakos Farmers Association

Market Information Management System built with Java, Spring Boot, Thymeleaf, and H2.

The application supports crop and market registration, price entry with duplicate and date validation, farmer registration, broadcasts, price lookup, market trends, and historical reports.

## Requirements

- Java 25 LTS or newer
- Maven 3.9 or newer
- Internet access for the first build so Maven can download dependencies

## Run the application

```bash
cd mims-java
mvn spring-boot:run
```

Then open [http://localhost:8080](http://localhost:8080).

You can also open the `mims-java` folder as a Maven project in IntelliJ IDEA, Eclipse, or VS Code and run `MimsApplication.java`.

## Database

The application uses a file-based H2 database by default; no separate database setup is needed. Data is stored in `mims-java/data/` and persists between restarts.

While the application is running, the H2 console is available at [http://localhost:8080/h2-console](http://localhost:8080/h2-console). Use JDBC URL `jdbc:h2:file:./data/mims`, username `sa`, and a blank password.

To switch to MySQL, uncomment the MySQL dependency in `mims-java/pom.xml`, then update the database settings in `mims-java/src/main/resources/application.properties`. Configure credentials outside source control before using the application with a non-local database.

## Accounts and password recovery

Farmers can create accounts with a username, email, password, phone number, and nearest market. Passwords are stored as BCrypt hashes. Administrator signup is not public; provision the first administrator by setting these environment variables before the first application start:

- `MIMS_ADMIN_USERNAME`
- `MIMS_ADMIN_EMAIL`
- `MIMS_ADMIN_PASSWORD` (at least 12 characters)

The administrator account is created only if the configured username/email do not already exist. Changing the environment password later does not overwrite an existing account.

Password recovery sends a single-use link that expires after 30 minutes. Configure SMTP outside source control before enabling email delivery:

- `SMTP_HOST`, `SMTP_PORT` (defaults to `587`)
- `SMTP_USERNAME`, `SMTP_PASSWORD`
- `SMTP_AUTH` and `SMTP_STARTTLS` (default to `true`)
- `MIMS_MAIL_FROM`
- `MIMS_PUBLIC_BASE_URL` (defaults to `http://localhost:8080`; set this to the public HTTPS app URL in deployment)

If SMTP is not configured, the recovery form reports that email delivery is unavailable.

## Using the application

1. On the landing page, choose **Administrator sign in** to manage crops, markets, prices, farmers, and broadcasts.
2. Choose **Look up today's prices** to access the farmer price lookup, trends, and alerts.
3. Administrators can test duplicate-price validation by entering the same crop, market, and date more than once.

## Project structure

```text
mims-java/
  src/main/java/ke/co/machakosfa/mims/
    model/        JPA entities
    repository/   Spring Data JPA repositories
    service/      Business logic and validation
    controller/   Spring MVC controllers
    exception/    Custom exceptions
  src/main/resources/
    templates/    Thymeleaf views
    static/css/   Stylesheet
    application.properties
  pom.xml
```
