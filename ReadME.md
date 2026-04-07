# SarniaBank — Queue Management System

A real-time customer queue management system built with Java Spring Boot. Customers can join a digital queue, admins can manage it from a dashboard, and a live display board keeps everyone updated.

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [How to Run Locally](#how-to-run-locally)
- [How to Run Tests](#how-to-run-tests)
- [Application Pages](#application-pages)
- [REST API Endpoints](#rest-api-endpoints)
- [Admin Login](#admin-login)
- [CI/CD Pipeline](#cicd-pipeline)
- [Azure VM Deployment](#azure-vm-deployment)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before running this application, make sure you have the following installed on your computer:

| Tool     | Version       | How to Check    | Download                              |
| -------- | ------------- | --------------- | ------------------------------------- |
| Java JDK | 17 or higher  | `java -version` | https://adoptium.net                  |
| Maven    | 3.8 or higher | `mvn -version`  | https://maven.apache.org/download.cgi |
| Git      | Any           | `git --version` | https://git-scm.com                   |

---

## Project Structure

```
manage_queue/
├── .github/
│   └── workflows/
│       └── ci-cd.yml               # GitHub Actions CI/CD pipeline
├── src/
│   ├── main/
│   │   ├── java/com/queue_manage/manage_queue/
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java       # Spring Security — protects admin page
│   │   │   ├── controller/
│   │   │   │   ├── QueueController.java      # REST API for queue operations
│   │   │   │   ├── AnalyticsController.java  # REST API for analytics/history
│   │   │   │   └── PageController.java       # Serves admin.html securely
│   │   │   ├── model/
│   │   │   │   ├── QueueEntry.java           # Queue entry entity (ticket, status, position)
│   │   │   │   └── Customer.java             # Customer entity (name, phone, email)
│   │   │   ├── repository/
│   │   │   │   └── QueueRepository.java      # Database queries with Spring Data JPA
│   │   │   ├── service/
│   │   │   │   └── QueueService.java         # All business logic
│   │   │   └── ManageQueueApplication.java   # Main entry point
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── index.html                # Customer join queue page
│   │       │   ├── admin.html                # Admin dashboard (protected)
│   │       │   ├── display.html              # Live public display board
│   │       │   ├── history.html              # Analytics and history
│   │       │   └── login.html                # Admin login page
│   │       └── application.properties        # App configuration
│   └── test/
│       └── java/com/queue_manage/manage_queue/
│           └── ManageQueueApplicationTests.java  # 15 automated tests
└── pom.xml                                   # Maven dependencies
```

---

## How to Run Locally

### Step 1 — Clone the repository

```bash
git clone https://github.com/deujarohan/DevOps-Project-Sarnia-Bank-Queue-Management-System-.git
cd manage_queue
```

### Step 2 — Build the project

```bash
mvn clean package -DskipTests
```

You should see `BUILD SUCCESS` at the end.

### Step 3 — Run the application

```bash
mvn spring-boot:run
```

Wait for this line in the terminal output:

```
Started ManageQueueApplication in X seconds
```

### Step 4 — Open in your browser

The application is now running at `http://localhost:8080`

| Page                | URL                                |
| ------------------- | ---------------------------------- |
| Join Queue          | http://localhost:8080/index.html   |
| Admin Dashboard     | http://localhost:8080/admin        |
| Display Board       | http://localhost:8080/display.html |
| History & Analytics | http://localhost:8080/history.html |
| H2 Database Console | http://localhost:8080/h2-console   |

### Step 5 — Stop the application

Press `Ctrl + C` in the terminal to stop the server.

---

## How to Run Tests

Make sure the application is **stopped** before running tests, then run:

```bash
mvn test
```

Expected output:

```
Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### What the tests cover

**Service Layer Tests (7 tests)**

- Join queue — customer added with correct ticket number
- Position assignment — multiple customers get correct positions
- Call next — first customer moves to SERVING status
- Serve customer — customer marked as SERVED with timestamp
- Skip customer — customer marked as SKIPPED
- Clear queue — all waiting customers cleared
- Ticket prefixes — G for General, A for Account, L for Loan, S for Support

**REST API Tests (8 tests)**

- POST /api/queue/join — valid join request returns ticket
- GET /api/queue/status — returns current waiting queue
- POST /api/queue/next — calls next customer
- GET /api/queue/stats — returns live stats
- DELETE /api/queue/clear — clears the queue
- GET /api/analytics/summary — returns analytics data
- Bad request handling — missing name returns 400 error
- Empty queue handling — returns empty array not error

---

## Application Pages

### Join Queue (`/index.html`)

- Customer enters their name
- Selects a service type (General, Account, Loan, Support)
- Clicks "Get my ticket" to join the queue
- Gets redirected to the display board automatically

### Admin Dashboard (`/admin`)

- **Requires login** — see Admin Login section below
- View all customers currently waiting in the queue
- See who is currently being served
- Click **Call next** to call the next customer
- Click **Mark as served** when done with a customer
- Click **Skip** to skip a customer
- Stats auto-refresh every 5 seconds
- Click **Clear queue** to remove all waiting customers

### Display Board (`/display.html`)

- Public-facing screen showing who is currently being served
- Lists the next customers in queue with their ticket numbers
- Auto-refreshes every 4 seconds — no manual reload needed
- Designed to be shown on a TV or large screen in the waiting area

### History & Analytics (`/history.html`)

- Total customers served and skipped
- Average wait time in minutes
- Service type breakdown with percentage bars
- Recent entries table showing last 10 customers

---

## REST API Endpoints

All API endpoints return JSON. Base URL: `http://localhost:8080`

### Customer Endpoints (Public)

| Method | Endpoint                 | Description           | Request Body                                         |
| ------ | ------------------------ | --------------------- | ---------------------------------------------------- |
| POST   | `/api/queue/join`        | Join the queue        | `{"customerName": "John", "serviceType": "GENERAL"}` |
| GET    | `/api/queue/status`      | Get waiting queue     | None                                                 |
| GET    | `/api/queue/stats`       | Get live stats        | None                                                 |
| GET    | `/api/queue/serving`     | Get currently serving | None                                                 |
| GET    | `/api/queue/ticket/{id}` | Get ticket by ID      | None                                                 |

### Service Types

| Value     | Description       |
| --------- | ----------------- |
| `GENERAL` | General Inquiry   |
| `ACCOUNT` | Account Services  |
| `LOAN`    | Loan & Mortgage   |
| `SUPPORT` | Technical Support |

### Admin Endpoints (Requires Authentication)

| Method | Endpoint                | Description                              |
| ------ | ----------------------- | ---------------------------------------- |
| POST   | `/api/queue/next`       | Call next customer                       |
| PUT    | `/api/queue/serve/{id}` | Mark customer as served                  |
| PUT    | `/api/queue/skip/{id}`  | Skip a customer                          |
| GET    | `/api/queue/all`        | Get all entries including served/skipped |
| DELETE | `/api/queue/clear`      | Clear all waiting customers              |

### Analytics Endpoints (Public)

| Method | Endpoint                 | Description                   |
| ------ | ------------------------ | ----------------------------- |
| GET    | `/api/analytics/summary` | Full analytics with breakdown |
| GET    | `/api/analytics/stats`   | Quick stats summary           |

### Example API Calls

```bash
# Join the queue
curl -X POST http://localhost:8080/api/queue/join \
  -H "Content-Type: application/json" \
  -d '{"customerName": "Jane Smith", "serviceType": "ACCOUNT"}'

# View the queue
curl http://localhost:8080/api/queue/status

# Get stats
curl http://localhost:8080/api/queue/stats
```

---

## Admin Login

The admin dashboard is protected with Spring Security. To access it:

1. Go to `http://localhost:8080/admin`
2. You will be redirected to the login page automatically
3. Enter the credentials:

| Field    | Value      |
| -------- | ---------- |
| Username | `admin`    |
| Password | `admin123` |

4. Click **Sign in to Admin**
5. To logout, click the **Logout** link in the top navigation

> **Note:** To change the admin password, edit `SecurityConfig.java` and update the `.password()` value. Rebuild and redeploy after changing.

---

## CI/CD Pipeline

The project uses GitHub Actions for automated build, test, and deployment.

### Pipeline file location

```
.github/workflows/ci-cd.yml
```

### Pipeline stages

**Stage 1 — Build**

- Runs on every push to `master` branch
- Sets up Java 17
- Runs `mvn clean package -DskipTests`
- Uploads the built JAR file as an artifact

**Stage 2 — Test**

- Runs only if Build stage passes
- Runs all 15 automated tests with `mvn test`
- Uploads test results report
- If any test fails, deployment is blocked

**Stage 3 — Deploy**

- Runs only if Test stage passes
- Only runs on pushes to `master` (not on pull requests)
- Downloads the JAR artifact
- Copies JAR to Azure VM using SCP
- SSH into VM and restarts the application

### Required GitHub Secrets

Set these in: **GitHub Repo → Settings → Secrets and variables → Actions**

| Secret Name        | Description                                             |
| ------------------ | ------------------------------------------------------- |
| `AZURE_VM_HOST`    | Public IP address of your Azure VM                      |
| `AZURE_VM_USER`    | VM username (e.g. `azureuser`)                          |
| `AZURE_VM_SSH_KEY` | Base64-encoded contents of your `.pem` private key file |

### How to encode the SSH key

On your computer, run:

```bash
cat your-key.pem | base64
```

Copy the output and paste it as the value for `AZURE_VM_SSH_KEY` in GitHub Secrets.

---

## Azure VM Deployment

### VM Configuration

| Setting  | Value                 |
| -------- | --------------------- |
| OS       | Ubuntu 24.04 LTS      |
| Size     | B1s (1 vCPU, 1GB RAM) |
| Region   | Canada East           |
| Java     | OpenJDK 17            |
| App Port | 8080                  |
| SSH Port | 22                    |

### Required Azure Firewall Rules

Go to **Azure Portal → Your VM → Networking → Inbound port rules** and ensure these rules exist:

| Port | Protocol | Source | Action |
| ---- | -------- | ------ | ------ |
| 22   | TCP      | Any    | Allow  |
| 8080 | TCP      | Any    | Allow  |

### Install Java on the VM (first time setup only)

SSH into your VM:

```bash
ssh -i [your-key].pem azureuser@[YOUR-VM-IP]
```

Then install Java 17:

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk
java -version
```

### Access the live application

After a successful pipeline deployment, the app is accessible at:

```
http://YOUR-VM-IP:8080/index.html
http://YOUR-VM-IP:8080/admin
http://YOUR-VM-IP:8080/display.html
http://YOUR-VM-IP:8080/history.html
```

### Check application logs on the VM

```bash
ssh -i your-key.pem azureuser@YOUR-VM-IP
cat ~/app/app.log
```

### Restart the application manually on the VM

```bash
ssh -i your-key.pem azureuser@YOUR-VM-IP
pkill -f 'java' || true
cd ~/app
nohup java -jar *.jar --server.port=8080 > app.log 2>&1 &
```

---

---

## Database

The application uses **H2 in-memory database** which means:

- Data is stored in memory while the app runs
- All data is lost when the app restarts
- No database setup required — it starts automatically

**Course:** CSD-4503 DevOps
**Deadline:** April 7, 2026
