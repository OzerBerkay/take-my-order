# Rules for take-my-order project

- Always clean up running background applications and background tasks (like `mvn spring-boot:run`) when finishing tasks or yielding control to the user so that ports (e.g. 8184) are freed up for local testing.
- **Exception Logging**: Always log expected Domain Exceptions and Authorization (Access Denied) errors at the `WARN` level. Do NOT include the full stack trace for these expected errors; only log the simple, human-readable message to prevent log noise.
- **Testing & Verification**: Always run `mvn test` or `mvn clean install` after making code changes. When introducing new rules or modifying existing logic, proactively write or update Unit Tests before finalizing the task.
- **Hibernate SQL Logging**: Always keep `spring.jpa.show-sql` disabled (`false`) across all microservices to reduce console noise unless explicitly asked to debug a query.
