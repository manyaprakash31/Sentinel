# Sentinel — Intelligent Cybersecurity Operations Platform

Sentinel is a production-grade, defensive Security Operations Center (SOC) and real-time security monitoring platform designed to ingest security telemetry, detect suspicious activity through configurable defensive rules, manage alerts and incidents, monitor infrastructure availability, and maintain an immutable administrative audit trail.

---

## 🛡️ Key Features

- **Genuine User Registration & RBAC:** Public operator registration assigning default `VIEWER` role with server-side validation and BCrypt hashing. Client-side role escalation is strictly rejected.
- **Secure Bootstrap Initial Admin:** Controlled bootstrap provisioning for the initial administrator requiring a secret token, eliminating hardcoded passwords.
- **Role-Based Access Control (RBAC):** Three-tier authorization across API endpoints, routes, and UI actions (`ADMIN`, `ANALYST`, `VIEWER`).
- **Defensive Brute-Force Rate Limiting:** In-memory sliding lockout protecting authentication endpoints against brute-force attacks.
- **Session Integrity & Session Verification:** Client verifies JWT token against the `/api/auth/me` endpoint on load/refresh; expired tokens automatically clear session state and redirect to login.
- **Clean Cybersecurity Authentication UI:** Pristine dark SOC login and registration interfaces without pre-filled credentials, hardcoded logins, or demo shortcuts.
- **Real-Time Security Telemetry Ingestion:** Safe defensive ingestion of application/perimeter logs (`LOGIN_FAILURE`, `ACCESS_DENIED`, `SERVICE_FAILURE`, `SUSPICIOUS_ACTIVITY`).
- **Defensive Detection Rule Engine:** Evaluates events against threshold & sliding time window policies (e.g., detecting $>5$ login failures within 5 minutes) to automatically trigger alerts.
- **Alert Triage & Incident Response:** Complete lifecycle workflows for alerts (`NEW` $\to$ `ACKNOWLEDGED` $\to$ `RESOLVED`) and incidents with chronological investigation notes.
- **Infrastructure Heartbeat Monitoring:** Live synthetic health checks tracking endpoint availability, response latency, and status (`UP`, `DEGRADED`, `DOWN`).
- **Immutable Audit Logging:** Captures all logins, rule modifications, incident status updates, and administrative actions.
- **WebSocket STOMP Realtime Updates:** Pushes live events, alert escalations, and service statuses directly to the browser without polling.

---

## 🏗️ Architecture

```
[ Security Event / Telemetry ]
               │
               ▼
   [ Spring Boot Ingestion API ]
               │
               ├───► [ WebSocket Broadcast ] ───► [ React SOC Dashboard ]
               │
               ▼
[ Defensive Detection Rule Engine ]
   (Sliding Time Window & Threshold)
               │
               ▼ (Threshold Breached)
       [ Security Alert ]
               │
               ▼ (Analyst Triage)
      [ Incident Response ]
   (Notes Timeline & Remediation)
               │
               ▼
     [ Immutable Audit Log ]
```

---

## 💻 Tech Stack

### Backend
- **Java 23 / 17** (Spring Boot 3.2.5)
- **Spring Web** & **Spring Data JPA** (Hibernate)
- **Spring Security** with **Stateless JWT (HMAC-SHA384)** & **BCrypt**
- **MySQL 8.0**
- **Jakarta Bean Validation** & **Lombok**
- **Spring WebSocket & STOMP Message Broker**

### Frontend
- **React 18** + **Vite 5**
- **Tailwind CSS** (Cybersecurity dark theme)
- **Recharts** (Interactive telemetry & severity metrics)
- **Axios** (Centralized JWT interceptors)
- **Lucide React** & **@stomp/stompjs**

---

## 🚀 Running the Project

### Prerequisites
- Java 17+ (e.g. `D:\JDK\jdk-23.0.2` or global `JAVA_HOME`)
- Node.js LTS (v18+)
- MySQL Server 8.0 running on `localhost:3306`

### 1. Database Configuration
Create the database in MySQL:
```sql
CREATE DATABASE sentinel_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Run Backend
```powershell
cd D:\Sentinel\backend
$env:JAVA_HOME = "D:\JDK\jdk-23.0.2"
.\mvnw.cmd spring-boot:run
```
*The backend runs on port `8085`: `http://localhost:8085`.*

### 3. Run Frontend
```powershell
cd D:\Sentinel\frontend
npm install
npm run dev
```
*The frontend dashboard runs on port `5173`: `http://localhost:5173`.*

---

## 🔑 Initial Administrator Setup (Bootstrap Process)

Sentinel does **not** hardcode a default administrator password. To provision your initial administrator safely:

Send a `POST` request to `/api/auth/bootstrap-admin` with the bootstrap secret configured in your environment (`BOOTSTRAP_SECRET`):

```powershell
$bootBody = @{
    bootstrapSecret = "SentinelBootstrap2026!SecureKey";
    username = "lead_admin";
    email = "admin@sentinel.sec";
    password = "AdminMaster@2026!";
    fullName = "Chief Information Security Officer"
} | ConvertTo-Json;

Invoke-RestMethod -Uri "http://localhost:8085/api/auth/bootstrap-admin" -Method Post -Body $bootBody -ContentType "application/json"
```

Once provisioned, the administrator can log in, access `/admin`, promote analysts, and manage detection rules. To permanently disable the bootstrap endpoint after initial setup, configure in `application.properties`:
```properties
sentinel.bootstrap.enabled=false
```

---

## 👥 Role Permissions Matrix

| Feature / Action | VIEWER | ANALYST | ADMIN |
| :--- | :---: | :---: | :---: |
| View Dashboard & Telemetry | ✅ | ✅ | ✅ |
| View Alerts & Incidents | ✅ | ✅ | ✅ |
| Acknowledge / Resolve Alerts | ❌ | ✅ | ✅ |
| Create / Update Incidents | ❌ | ✅ | ✅ |
| Add Incident Investigation Notes | ❌ | ✅ | ✅ |
| Create / Edit / Delete Detection Rules | ❌ | ❌ | ✅ |
| Manage Monitored Services | ❌ | ❌ | ✅ |
| Manage Users & Roles (RBAC) | ❌ | ❌ | ✅ |
| View Security Audit Logs | ❌ | ❌ | ✅ |

---

## 📡 API Overview

| Method | Endpoint | Description | Permitted Roles |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Authenticate user & retrieve JWT | Public |
| `POST` | `/api/auth/register` | Register new account (default VIEWER) | Public |
| `POST` | `/api/auth/bootstrap-admin` | Secure one-time admin provisioning | Secret Protected |
| `GET` | `/api/auth/me` | Fetch authenticated operator details | Authenticated |
| `GET` | `/api/auth/users` | List SOC personnel | `ADMIN` |
| `PUT` | `/api/auth/users/{id}/role` | Promote/change user role | `ADMIN` |
| `PUT` | `/api/auth/users/{id}/status` | Enable/disable user account | `ADMIN` |
| `POST` | `/api/events/ingest` | Safe telemetry ingestion endpoint | Public |
| `GET` | `/api/events` | Paginated search of security events | Authenticated |
| `GET` | `/api/alerts` | Filtered security alerts list | Authenticated |
| `POST` | `/api/alerts/{id}/acknowledge` | Acknowledge alert | `ANALYST`, `ADMIN` |
| `POST` | `/api/alerts/{id}/resolve` | Resolve alert | `ANALYST`, `ADMIN` |
| `GET` | `/api/incidents` | Incident response list | Authenticated |
| `POST` | `/api/incidents` | Declare new incident | `ANALYST`, `ADMIN` |
| `POST` | `/api/incidents/{id}/notes` | Append timeline note | `ANALYST`, `ADMIN` |
| `GET` | `/api/rules` | Configurable detection rules | Authenticated |
| `POST` | `/api/rules` | Create detection rule | `ADMIN` |
| `GET` | `/api/services` | Infrastructure availability health | Authenticated |
| `GET` | `/api/audit-logs` | Immutable audit trail | `ADMIN` |
| `GET` | `/api/dashboard/summary` | Real-time aggregated SOC metrics | Authenticated |

---

## 🧪 Postman Collection
A complete Postman collection is included under:
`D:\Sentinel\docs\Sentinel_API_Collection.postman_collection.json`
