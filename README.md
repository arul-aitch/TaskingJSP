# TaskingJSP

Tasking — task manager — migrated from Laravel to **Java 17 + Jakarta Servlet 6 + JSP/JSTL + JDBC + PostgreSQL**, targeting **Apache Tomcat 10.1**.

## Project structure

```
TaskingJSP/
├── pom.xml                     Maven WAR build (Java 17)
├── README.md
├── docs/
│   ├── architecture.md
│   ├── migration-mapping.md
│   ├── database-compatibility.md
│   ├── security-review.md
│   └── testing-report.md
└── src/main/
    ├── java/com/tasking/
    │   ├── controller/   HomeServlet, AuthServlet, DashboardServlet, TaskServlet,
    │   │                 CategoryServlet, SubtaskServlet, ProfileServlet, PasswordResetServlet
    │   ├── dao/          UserDAO, CategoryDAO, TaskDAO, SubtaskDAO
    │   ├── filter/       AuthFilter (auth + CSRF)
    │   ├── model/        User, Category, Task, Subtask
    │   └── util/         DBConnection, PasswordUtil, CSRFUtil, ValidationUtil
    ├── resources/
    │   └── db.properties.example
    └── webapp/
        ├── assets/css/app.css, assets/js/app.js
        ├── index.jsp
        └── WEB-INF/
            ├── web.xml
            ├── jsp/ (layout fragments, sidebar)
            └── views/ (onboarding, dashboard, profile, auth/*, error/*)
```

## Build

Requirements: JDK 17, Maven 3.8+.

```bash
mvn clean package
# → target/TaskingJSP.war
```

## Configure PostgreSQL

Do **not** run any migration — the app uses your existing database.

Option A — environment variables (recommended):
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=tasking
export DB_USER=your_user
export DB_PASSWORD=your_password
```

Option B — properties file: copy `src/main/resources/db.properties.example` to `src/main/resources/db.properties` and fill values (env vars take precedence).

Schema verification queries: `docs/database-compatibility.md`.

## Run in Eclipse
1. File → Import → Existing Maven Projects → select the `TaskingJSP` folder.
2. Right-click project → Run As → Maven install (builds the WAR).
3. Servers view → create a **Tomcat 10.1** server (needs Jakarta EE 9+; Tomcat 9 will NOT work).
4. Add project to the server (or drag the WAR onto it), Start.
5. Open `http://localhost:8080/TaskingJSP/`.

## Run on standalone Tomcat 10.1
```bash
cp target/TaskingJSP.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/startup.sh
# http://localhost:8080/TaskingJSP/
```

## Features
- Onboarding landing page; register/login/logout with session fixation protection and login throttling
- Forgot/reset password flow (session-based token; SMTP integration point documented — no mail is sent until configured)
- Dashboard: All Tasks / Archive, progress counters, search (ILIKE), category filter, sorting (nearest deadline / newest), pinned-first ordering
- Task CRUD via modals; complete toggle; pin/unpin; deadline badges (overdue/urgent/normal)
- Subtasks: add, toggle, delete, progress bar
- Category CRUD in sidebar with edit/delete modals; delete sets tasks' category to NULL
- Profile: update name/email (email change resets verification), change password, delete account (password-confirmed, cascades)
- Security: bcrypt ($2y$ compatible with Laravel hashes), CSRF on all POSTs, ownership checks on every operation, prepared statements, output escaping, PRG flow

## Known limitations
- "Remember me" stores a cookie hint only (no persistent-login token table exists in the original schema)
- Email delivery (verification / password reset mail) requires a configured SMTP/mail session; placeholders documented
- Testing report: see `docs/testing-report.md` for exactly what was verified vs. NOT TESTED in the authoring environment
