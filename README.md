# Loan Approval System

A backend (+ simple frontend) for handling loan applications.

---

## Flow

Customers can submit loan applications, the system validates them, generates a payment schedule, and then a reviewer can approve or reject the application.

**The flow looks like this:**

```
Customer submits application
        ↓
Age check (auto-reject if too old)
        ↓
Payment schedule generated (annuity)
        ↓
Status: IN_REVIEW
        ↓
Reviewer approves or rejects
        ↓
Status: APPROVED or REJECTED
```

---

## Tech stack

| Thing | What |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot |
| Database | PostgreSQL |
| Migrations | Liquibase |
| API docs | OpenAPI 3 / Swagger |
| Frontend | React + TypeScript (Vite) |

---

## How to run it

```
git clone <repo-url>
cd loan_approval_system
docker-compose up -d
```

- **Frontend** → http://localhost:3000
- **Backend API** → http://localhost:8080
- **Swagger UI** → http://localhost:8080/swagger-ui.html

---

## API endpoints

All endpoints live under `/api/loan-applications`.

| Method | Path | What it does |
|---|---|---|
| `POST` | `/` | Submit a new loan application |
| `GET` | `/` | Get all applications currently in review |
| `POST` | `/{id}/approve` | Approve an application |
| `POST` | `/{id}/reject` | Reject an application (with a reason) |
| `GET` | `/{id}/payment-schedule` | Get the payment schedule for an application |
| `POST` | `/{id}/regenerate-schedule` | Recalculate the schedule with new params |
| `GET` | `/approved` | Get all approved applications by personal code |

---

## Loan application fields

When submitting a new application, these fields are required:

| Field | Rules |
|---|---|
| `firstName` | max 32 chars |
| `lastName` | max 32 chars |
| `personalCode` | valid Estonian ID code (11 digits) |
| `loanPeriodMonths` | 6–360 months |
| `interestMargin` | ≥ 0.1% |
| `baseInterest` | ≥ 0.1% (6-month Euribor) |
| `loanAmount` | min €5000 |

One person can only have one active application at a time.

---

## Business rules

- If the applicant is **older than 70**, the application is automatically rejected with reason `CUSTOMER_TOO_OLD`
- The age limit is configurable in `application.properties` (`loan.max-age`)
- The default base interest rate is also configurable (`loan.base-interest`)
- Payment schedule uses the **annuity method** (equal monthly payments)
- First payment date defaults to today

---

## What's implemented

- Loan application submission with validation
- Age check with auto-rejection
- Annuity payment schedule generation
- Approve / reject flow
- Global error handling (`@RestControllerAdvice`)
- Unit tests with Mockito
- Dynamic config via database (`loan_config` table)
- Schedule regeneration in `IN_REVIEW` state
- Simple React frontend UI
