# MIMS — Machakos Farmers Association
Market Information Management System — Java (Spring Boot) + HTML (Thymeleaf) + CSS

## What this is
A server-rendered web application implementing the objectives from your project report:
crop & market registration, price entry with duplicate/timestamp validation,
farmer registration, broadcast dispatch, price lookup, market trends, and
exportable historical reports.

## Requirements
- Java 17 or newer (`java -version`)
- Maven 3.9+ (`mvn -version`) — or use an IDE that bundles it (IntelliJ, Eclipse, VS Code + Java extensions)
- Internet access the first time you build (Maven needs to download dependencies from Maven Central)

## Running it

### Option A — command line
```bash
cd mims-java
mvn spring-boot:run
```
Then open **http://localhost:8080** in your browser.

### Option B — an IDE
1. Open the `mims-java` folder as a Maven project in IntelliJ IDEA, Eclipse, or VS Code.
2. Let it download dependencies (first time only).
3. Run `MimsApplication.java` (it has a `main` method) or use the IDE's "Run" button.
4. Open **http://localhost:8080**.

## Database
Uses an embedded H2 database by default — **no setup required**. Data is saved to
a `data/` folder next to the project and persists between restarts.

To browse the database directly, visit **http://localhost:8080/h2-console** while the
app is running (JDBC URL: `jdbc:h2:file:./data/mims`, username `sa`, no password).

### Switching to MySQL or PostgreSQL
Your report names MySQL/PostgreSQL as project tools, so the project ships ready to
switch:
1. In `pom.xml`, uncomment the `mysql-connector-j` dependency.
2. In `src/main/resources/application.properties`, comment out the H2 block and
   uncomment the MySQL block, filling in your database name/username/password.
3. Create the database (or let `createDatabaseIfNotExist=true` do it for you) and restart.

## How to use it
1. On the landing page, choose **Administrator sign in** to register crops, markets,
   post prices, register farmers, and dispatch broadcasts.
2. Choose **Look up today's prices** to sign in as a farmer and browse prices, trends,
   and alerts.
3. Try posting the same crop/market/date price twice as an administrator — the
   duplicate-validation rule from Objective II will catch it and offer to replace
   the existing record instead of silently creating a second one.

## Project structure
```
src/main/java/ke/co/machakosfa/mims/
  model/        JPA entities: Crop, Market, Farmer, PriceRecord, Broadcast
  repository/   Spring Data JPA repositories
  service/      Business logic — PriceService holds the validation rules
  controller/   Spring MVC controllers that serve the HTML views
  exception/    Custom exceptions for duplicate/invalid price entries
src/main/resources/
  templates/    Thymeleaf HTML views (server-rendered)
  static/css/   Stylesheet
  application.properties
```
