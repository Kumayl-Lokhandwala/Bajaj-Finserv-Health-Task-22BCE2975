# Bajaj Finserv Health – Java Webhook Task (Spring Boot)

This project is my solution for the Bajaj Finserv Health Java qualifier task.  
The application automatically runs on startup, generates a webhook, solves the assigned SQL question, and submits the final SQL query using the returned access token.

---

## How It Works

1. On application startup, `StartupRunner` triggers the process automatically.
2. `WebhookService` sends a POST request to generate a webhook and access token.
3. The SQL problem is solved inside the service layer.
4. The final SQL query is submitted to the webhook URL using the JWT token.
5. Response from the server confirms successful submission.

---

## Final SQL Query Used

```sql
SELECT
    d.DEPARTMENT_NAME,
    emp_totals.total_salary AS SALARY,
    CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS EMPLOYEE_NAME,
    TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE
FROM (
    SELECT
        p.EMP_ID,
        SUM(p.AMOUNT) AS total_salary,
        e.DEPARTMENT
    FROM PAYMENTS p
    JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID
    WHERE DAY(p.PAYMENT_TIME) <> 1
    GROUP BY p.EMP_ID, e.DEPARTMENT
) emp_totals
JOIN (
    SELECT
        DEPARTMENT,
        MAX(total_salary) AS max_salary
    FROM (
        SELECT
            p.EMP_ID,
            SUM(p.AMOUNT) AS total_salary,
            e.DEPARTMENT
        FROM PAYMENTS p
        JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID
        WHERE DAY(p.PAYMENT_TIME) <> 1
        GROUP BY p.EMP_ID, e.DEPARTMENT
    ) t
    GROUP BY DEPARTMENT
) dept_max ON emp_totals.DEPARTMENT = dept_max.DEPARTMENT
           AND emp_totals.total_salary = dept_max.max_salary
JOIN EMPLOYEE e ON emp_totals.EMP_ID = e.EMP_ID
JOIN DEPARTMENT d ON emp_totals.DEPARTMENT = d.DEPARTMENT_ID;

```

## How to Run the Project

### 1. Run with Maven

```bash
mvn spring-boot:run
```

### 2. Run with the JAR file

Download the JAR from GitHub Releases, then run:

```bash
java -jar bh-qualifier-0.0.1-SNAPSHOT.jar
```

Expected Output:

```bash
Webhook generated
Access token received
Final query submitted
{"success": true, "message": "Webhook processed successfully"}
Flow completed.
```

## Download JAR File

The final JAR is available under GitHub Releases:

https://github.com/Kumayl-Lokhandwala/Bajaj-Finserv-Health-Task-22BCE2975/releases/tag/v1.0

Repository Link
https://github.com/Kumayl-Lokhandwala/Bajaj-Finserv-Health-Task-22BCE2975
