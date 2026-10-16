# MIMS — Machakos Farmers Association

Market Information Management System built with Java, Spring Boot, Thymeleaf, and H2.

The application supports crop and market registration, price entry with duplicate and date validation, farmer registration, broadcasts, price lookup, market trends, and historical reports.

## Requirements

- Java 17 or newer
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
