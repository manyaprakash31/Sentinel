# Sentinel — Intelligent Cybersecurity Operations Platform

Sentinel is a production-grade, defensive Security Operations Center (SOC) and real-time security monitoring platform designed to ingest security telemetry, detect suspicious activity through configurable defensive rules, manage alerts and incidents, monitor infrastructure availability, and maintain an immutable administrative audit trail.

---

## 🛡️ Key Features

- **Real-Time Security Telemetry Ingestion:** Safe defensive ingestion of application/perimeter logs (`LOGIN_FAILURE`, `ACCESS_DENIED`, `SERVICE_FAILURE`, `SUSPICIOUS_ACTIVITY`).
- **Defensive Detection Rule Engine:** Evaluates events against threshold & sliding time window policies (e.g., detecting $>5$ login failures within 5 minutes) to automatically trigger alerts.
- **Alert Triage & Lifecycle:** Severity categorization (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`) and analyst workflow (`NEW` $\to$ `ACKNOWLEDGED` $\to$ `INVESTIGATING` $\to$ `RESOLVED`).
- **Incident Response Management:** Complete incident lifecycle with chronological analyst investigation note tracking and alert association.
- **Infrastructure Heartbeat Monitoring:** Live synthetic health checks tracking endpoint availability, response latency, and status (`UP`, `DEGRADED`, `DOWN`).
- **Immutable Audit Logging:** Captures all logins, rule modifications, incident status updates, and administrative actions.
- **Modern SOC Interface:** Dark-first cybersecurity aesthetics designed with Tailwind CSS, Lucide icons, and Recharts.
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
- Java 17+ (configured via `D:\JDK\jdk-23.0.2` or global `JAVA_HOME`)
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
*The backend starts at `http://localhost:8085`.*

### 3. Run Frontend
```powershell
cd D:\Sentinel\frontend
npm install
npm run dev
```
*The frontend dashboard opens at `http://localhost:5173`.*

---

## 🔑 Demo Credentials

| Role | Username | Password | Access Capabilities |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `Admin@123` | Full access: rules, audit logs, users, incidents |
| **SOC Analyst** | `analyst` | `Analyst@123` | Triage alerts, declare & update incidents, post notes |
| **Auditor / Viewer** | `viewer` | `Viewer@123` | Read-only access to dashboards, events & services |

---

## 📡 API Overview

| Method | Endpoint | Description | Role |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Authenticate user & retrieve JWT | Public |
| `POST` | `/api/auth/register` | Register new user | Public / Admin |
| `POST` | `/api/events/ingest` | Safe telemetry ingestion endpoint | Public |
| `GET` | `/api/events` | Paginated search of security events | Authenticated |
| `GET` | `/api/alerts` | Filtered security alerts list | Authenticated |
| `POST` | `/api/alerts/{id}/acknowledge` | Acknowledge alert | Analyst / Admin |
| `POST` | `/api/alerts/{id}/resolve` | Resolve alert | Analyst / Admin |
| `GET` | `/api/incidents` | Incident response list | Authenticated |
| `POST` | `/api/incidents` | Declare new incident | Analyst / Admin |
| `POST` | `/api/incidents/{id}/notes` | Append timeline note | Analyst / Admin |
| `GET` | `/api/rules` | Configurable detection rules | Analyst / Admin |
| `GET` | `/api/services` | Infrastructure availability health | Authenticated |
| `GET` | `/api/audit-logs` | Immutable audit trail | Admin |
| `GET` | `/api/dashboard/summary` | Real-time aggregated SOC metrics | Authenticated |

---

## 🧪 Postman Collection
A complete Postman collection is included under:
`D:\Sentinel\docs\Sentinel_API_Collection.postman_collection.json`
