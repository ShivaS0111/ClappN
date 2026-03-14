# 🛠️ Developer Setup & Onboarding Guide

**Last Updated**: March 14, 2026  
**Level**: Beginner-friendly  
**Time Required**: 30-45 minutes

---

## Table of Contents
1. [System Requirements](#system-requirements)
2. [Initial Setup](#initial-setup)
3. [Frontend Setup](#frontend-setup)
4. [Backend Setup](#backend-setup)
5. [Database Setup](#database-setup)
6. [Verification](#verification)
7. [Development Workflow](#development-workflow)
8. [Useful Commands](#useful-commands)
9. [Troubleshooting](#troubleshooting)
10. [API Testing](#api-testing)

---

## System Requirements

### Minimum Requirements
- **OS**: Windows 10/11, macOS 11+, or Ubuntu 20.04+
- **RAM**: 8GB (16GB recommended)
- **Storage**: 10GB free space
- **CPU**: Dual-core processor

### Required Software

#### Frontend Development
- **Node.js**: v18.0.0 or higher
  ```bash
  # Check version
  node --version
  npm --version
  ```
  - Download: https://nodejs.org/

- **Git**: v2.30.0 or higher
  ```bash
  git --version
  ```
  - Download: https://git-scm.com/

- **Text Editor**: 
  - VS Code (recommended)
  - IntelliJ IDEA
  - Sublime Text

#### Backend Development
- **Java**: JDK 17 or higher
  ```bash
  java -version
  ```
  - Download: https://adoptopenjdk.net/

- **Maven**: v3.6.0 or higher
  ```bash
  mvn --version
  ```
  - Download: https://maven.apache.org/

- **IDE**: 
  - IntelliJ IDEA Community/Ultimate (recommended)
  - Eclipse
  - VS Code with Java extension

#### Database
- **MySQL**: v8.0 or higher
  - Download: https://dev.mysql.com/downloads/mysql/
  - Or use Docker: `docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root mysql:8.0`

### Optional but Recommended
- **Docker**: For containerization
- **Postman**: For API testing
- **MySQL Workbench**: For database management
- **Git GUI**: GitKraken or SourceTree

---

## Initial Setup

### Step 1: Clone Repository

```bash
# Navigate to desired location
cd d:/projects  # or your preferred directory

# Clone ClappN (Backend)
git clone https://github.com/your-org/ClappN.git
cd ClappN

# Clone clapp-dashboard (Frontend)
cd ..
git clone https://github.com/your-org/clapp-dashboard.git
```

### Step 2: Set Git Configuration

```bash
# Set your git identity
git config --global user.name "Your Name"
git config --global user.email "your.email@company.com"

# Configure line endings (important for cross-platform dev)
git config --global core.autocrlf true  # Windows
git config --global core.autocrlf input  # macOS/Linux
```

### Step 3: Create Development Branch

```bash
# Backend
cd ClappN
git checkout -b develop
git pull origin develop

# Frontend
cd ../clapp-dashboard
git checkout -b develop
git pull origin develop
```

---

## Frontend Setup

### Step 1: Install Dependencies

```bash
cd clapp-dashboard
npm install

# Or use yarn
yarn install
```

**What it does**:
- Downloads all packages from `package.json`
- Creates `node_modules/` directory
- Generates `package-lock.json`

### Step 2: Environment Configuration

Create `.env` file in project root (or copy from `.env.example`):

```env
# .env.local (for local development)
VITE_API_BASE_URL=http://localhost:8080
VITE_API_TIMEOUT=30000
VITE_JWT_STORAGE_KEY=clapp_jwt_token
VITE_REFRESH_TOKEN_KEY=clapp_refresh_token

# Optional
VITE_ENV=development
VITE_DEBUG_MODE=true
```

### Step 3: Start Development Server

```bash
# Option 1: Standard dev server
npm run dev

# Option 2: With specific port
npm run dev -- --port 5173

# Output: 
# ➜  Local:   http://localhost:5173/
```

**Verify**: Open http://localhost:5173 in browser

### Step 4: Verify Installation

```bash
# Check build
npm run build

# Lint check
npm run lint

# Expected output: No errors
```

### Common Frontend Commands

```bash
# Development
npm run dev                 # Start dev server

# Production
npm run build              # Build for production
npm run preview            # Preview built version

# Code Quality
npm run lint               # Run ESLint
npm run lint:fix           # Auto-fix lint errors
npm run format             # Format with Prettier

# Testing
npm run test               # Run unit tests
npm run test:watch        # Watch mode

# Analysis
npm run analyze            # Bundle size analysis
npm run typescript-check   # TypeScript check
```

---

## Backend Setup

### Step 1: Maven Dependencies

```bash
cd ClappN

# Download all dependencies (takes 2-3 minutes first time)
mvn clean install

# Verify installation
mvn -version

# Expected output:
# Apache Maven X.X.X
# Java version: 17.X.X
```

### Step 2: Environment Configuration

Create `application-dev.properties` (if doesn't exist already):

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/clapp
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# JWT Configuration
jwt.secret=your-secret-key-min-32-characters-long-here
jwt.expiration=86400000  # 1 day in milliseconds

# Logging
logging.level.root=INFO
logging.level.biz.craftline=DEBUG
logging.file.name=logs/application.log

# Profile
spring.profiles.active=dev
```

### Step 3: Create Database

```bash
# Using MySQL command line
mysql -u root -p -e "CREATE DATABASE clapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p clapp < db/startup.sql

# Verify
mysql -u root -p -e "USE clapp; SHOW TABLES;"
```

### Step 4: Run Backend Application

```bash
# Option 1: Maven
mvn clean spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Option 2: From IntelliJ IDEA
# Right-click ClappnApplication.java → Run

# Verify: Open http://localhost:8080/swagger-ui.html
```

**Expected Output**:
```
Started ClappnApplication in X.XXX seconds
```

### Backend Startup Checklist
- ✅ Server started on port 8080
- ✅ Database connected
- ✅ Flyway migrations completed
- ✅ Swagger UI accessible at `/swagger-ui.html`
- ✅ No errors in console

---

## Database Setup

### Option 1: Manual Setup

```bash
# Connect to MySQL
mysql -u root -p

# Create database
CREATE DATABASE clapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE clapp;

# Run initialization script
SOURCE db/startup.sql;
SOURCE db/categories.sql;
SOURCE db/role-permissions-data.sql;
SOURCE db/business-types-data.sql;

# Verify
SHOW TABLES;
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM permissions;
```

### Option 2: Docker Setup

```bash
# Start MySQL container
docker run -d \
  --name mysql-clapp \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=clapp \
  -v mysql_data:/var/lib/mysql \
  mysql:8.0

# Wait for container to start (10 seconds)
sleep 10

# Import data
docker exec -i mysql-clapp mysql -u root -proot clapp < db/startup.sql

# Verify
docker exec mysql-clapp mysql -u root -proot clapp -e "SHOW TABLES;"
```

### Database Tables Overview

```
Core Tables (30+):
├── Users & Auth          (users, roles, permissions, user_roles)
├── Business              (businesses, stores, business_types)
├── Products              (products, categories, product_lots, vendors)
├── Services              (services, service_bookings)
├── Orders                (orders, order_items, invoices)
├── Payments              (payments, payment_transactions)
├── Marketing             (offers, coupons, discounts)
├── Approvals             (approvals, approval_comments)
├── Audit                 (activity_logs, audit_trails)
└── System                (settings, configurations)
```

### Database Backup

```bash
# Create backup
mysqldump -u root -p clapp > backup_`date +%Y%m%d_%H%M%S`.sql

# Restore from backup
mysql -u root -p clapp < backup_20240314_120000.sql
```

---

## Verification

### Frontend Verification

```bash
# 1. Check if dev server is running
curl http://localhost:5173

# 2. Check console for no TypeScript errors
# Open VS Code Problems panel: Ctrl+Shift+M (Windows) / Cmd+Shift+M (Mac)

# 3. Run build test
npm run build
# Should complete with no errors

# 4. Test a simple request
# Open DevTools Console and try:
fetch('http://localhost:8080/api/auth/me', {
  headers: { 'Authorization': 'Bearer test' }
})
```

### Backend Verification

```bash
# 1. Check if backend is running
curl http://localhost:8080

# 2. Check Swagger UI
curl http://localhost:8080/swagger-ui.html

# 3. Test database connection
curl http://localhost:8080/api/health

# Expected response:
# {"status": "UP", "database": "UP"}

# 4. Check logs
tail -f logs/application.log
```

### Full Stack Verification

```bash
# 1. Frontend + Backend running
# Frontend: http://localhost:5173
# Backend: http://localhost:8080
# Database: mysql://localhost:3306

# 2. Try login with test credentials
# Email: admin@system.com
# Password: admin123

# 3. Verify in Network tab:
# Request to: http://localhost:8080/api/auth/login
# Response has: accessToken in data
```

---

## Development Workflow

### Daily Workflow

```
1. Morning: Start your day
   └─ Update code from main branch
   └─ Pull latest changes: git pull
   └─ Install new dependencies: npm install / mvn clean install
   
2. Development: Work on task
   └─ Create feature branch: git checkout -b feature/task-name
   └─ Make code changes
   └─ Test changes locally
   └─ Keep dev server running
   
3. Testing: Before commit
   └─ Run linter: npm run lint
   └─ Run tests: npm run test
   └─ Build check: npm run build
   └─ Manual testing in browser
   
4. Commit: Save your work
   └─ Stage changes: git add .
   └─ Commit: git commit -m "feat: descriptive message"
   └─ Push: git push origin feature/task-name
   
5. Review: Submit for review
   └─ Create Pull Request
   └─ Request reviewers
   └─ Address feedback
   └─ Merge to develop
```

### Feature Development Steps

```bash
# 1. Create feature branch
git checkout -b feature/user-authentication

# 2. Make changes to files
# Edit src/features/auth/AuthProvider.tsx
# Edit src/features/auth/hooks/useAuth.ts
# ...

# 3. Test locally
npm run dev            # Frontend
mvn spring-boot:run    # Backend

# 4. Verify changes
npm run lint
npm run build

# 5. Commit changes
git add src/features/auth/
git commit -m "feat: implement user authentication flow"

# 6. Push to remote
git push origin feature/user-authentication

# 7. Create Pull Request on GitHub
```

### Debugging Tips

#### Frontend Debugging
```bash
# 1. React DevTools Browser Extension
# Install: https://react-devtools-tutorial.vercel.app/

# 2. Browser DevTools
# Open: F12 or Right-click → Inspect
# Check: Network, Console, Application tabs

# 3. VS Code Debugger
# .vscode/launch.json:
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "chrome",
      "request": "launch",
      "name": "Launch Chrome",
      "url": "http://localhost:5173",
      "webRoot": "${workspaceFolder}/src",
      "sourceMaps": true
    }
  ]
}

# 4. Console logging
console.log('Debug:', variable);
console.table(array);
console.time('operation');
// ... code
console.timeEnd('operation');
```

#### Backend Debugging
```bash
# 1. Add breakpoint in IntelliJ
# Click on line number to set breakpoint

# 2. Run in debug mode
# In IntelliJ: Shift+F9 or Run → Debug

# 3. Inspect variables
# Hover over variable or use Variables panel

# 4. Check logs
tail -f ClappN/logs/application.log

# 5. Use Postman for API testing
# Import: ClappN/postman/ClappN.postman_collection.json
```

---

## Useful Commands

### Frontend Commands

```bash
# Installation
npm install              # Install dependencies
npm install -D pkg-name  # Install dev dependency
npm uninstall pkg-name   # Remove package

# Development
npm run dev              # Start dev server
npm run build            # Build for production
npm run preview          # Preview production build

# Code Quality
npm run lint             # Check for errors
npm run format           # Format code
npm run type-check       # TypeScript check

# Testing
npm run test             # Run tests
npm run test:watch      # Watch mode
npm run test:coverage    # Coverage report

# Package Management
npm list                 # List all packages
npm outdated             # Check for updates
npm update               # Update packages
```

### Backend Commands

```bash
# Maven
mvn clean install        # Clean & install
mvn compile              # Compile only
mvn test                 # Run tests
mvn package              # Create JAR
mvn spring-boot:run      # Run application
mvn clean spring-boot:run # Clean run

# Database
mvn flyway:migrate       # Run migrations
mvn flyway:info          # Show migration status
mvn flyway:undo          # Undo last migration

# Analysis
mvn checkstyle:check     # Code style check
mvn spotbugs:check       # Bug detection
```

### Git Commands

```bash
# Branches
git branch               # List branches
git branch -a            # List all branches
git checkout -b feat     # Create & switch to branch
git branch -d branch     # Delete branch
git branch -m old new    # Rename branch

# Commits
git status               # Check status
git add .                # Stage all changes
git add file.txt         # Stage specific file
git commit -m "message"  # Commit
git push                 # Push to remote
git pull                 # Fetch & merge

# History
git log                  # Show commit history
git log --oneline -10    # Last 10 commits
git diff                 # Show unstaged changes
git log -p file.txt      # Show file history

# Stashing
git stash                # Temporarily save changes
git stash list           # List stashes
git stash pop            # Apply last stash
```

### MySQL Commands

```bash
# Connection
mysql -u root -p         # Connect to MySQL
mysql -u root -p clapp   # Connect to database

# Database Operations
SHOW DATABASES;          # List databases
USE clapp;              # Switch database
SHOW TABLES;            # List tables
DESCRIBE table_name;    # Show table structure

# Data Operations
SELECT * FROM users LIMIT 10;  # View data
UPDATE users SET status=1 WHERE id=1;
DELETE FROM table_name WHERE condition;

# Backup & Restore
mysqldump -u root -p db_name > backup.sql
mysql -u root -p db_name < backup.sql
```

---

## Troubleshooting

### Frontend Issues

**Problem**: `npm install` fails
```bash
# Solution 1: Clear npm cache
npm cache clean --force
npm install

# Solution 2: Delete node_modules and reinstall
rm -rf node_modules package-lock.json
npm install

# Solution 3: Use npm ci (for CI/CD)
npm ci
```

**Problem**: Port 5173 already in use
```bash
# Solution: Run on different port
npm run dev -- --port 5174

# Or kill the process
# Windows
netstat -ano | findstr :5173
taskkill /PID <PID> /F

# macOS/Linux
lsof -i :5173
kill -9 <PID>
```

**Problem**: CORS errors in browser console
```
Solution: Ensure backend is running on port 8080
Check: VITE_API_BASE_URL in .env is set correctly
Check: Backend CORS configuration
```

### Backend Issues

**Problem**: `mvn clean install` takes too long
```bash
# Solution: Skip tests
mvn clean install -DskipTests

# Solution: Use offline mode
mvn clean install -o
```

**Problem**: Database connection refused
```bash
# Check MySQL is running
# Windows: Services → MySQL80
# Mac: brew services list
# Linux: sudo systemctl status mysql

# Check connection string
spring.datasource.url=jdbc:mysql://localhost:3306/clapp

# Test connection
mysql -u root -p -e "USE clapp; SHOW TABLES;"
```

**Problem**: Port 8080 already in use
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# macOS/Linux
lsof -i :8080
kill -9 <PID>

# Or configure different port in application.properties
server.port=8081
```

**Problem**: Flyway migration fails
```bash
# Check migrations folder
ls db/migration/

# Manually reset (careful!)
# 1. Drop database: DROP DATABASE clapp;
# 2. Recreate: CREATE DATABASE clapp;
# 3. Re-run: mvn spring-boot:run
```

### Database Issues

**Problem**: Authentication error
```bash
# Check credentials in application.properties
# Default: root / root

# Reset password
mysql -u root -p
ALTER USER 'root'@'localhost' IDENTIFIED BY 'newpassword';
FLUSH PRIVILEGES;
```

**Problem**: Tables don't exist
```bash
# Run init scripts manually
mysql -u root -p clapp < db/startup.sql
mysql -u root -p clapp < db/categories.sql

# Or delete database and restart backend (Flyway will recreate)
```

---

## API Testing

### Using Postman

```bash
# 1. Import Collection
# Open Postman → Import → Select ClappN/postman/ClappN.postman_collection.json

# 2. Set Environment Variables
# Click "Environments" → "Create New"
# Add variables:
{
  "base_url": "http://localhost:8080",
  "jwt_token": "your-token-here"
}

# 3. Run Requests
# Select request and click "Send"

# 4. Save responses for reference
# Click "Save" to save as preset
```

### Using cURL

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@system.com","password":"admin123"}'

# Get Orders (with JWT)
curl -X GET http://localhost:8080/api/order \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Create Order
curl -X POST http://localhost:8080/api/order \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":1,"quantity":2}],"gstRate":18}'
```

### Using REST Client (VS Code)

Create `requests.http` file:

```http
### Authenticate
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@system.com",
  "password": "admin123"
}

### Get Orders
GET http://localhost:8080/api/order
Authorization: Bearer <insert-jwt-token>

### Create Order
POST http://localhost:8080/api/order
Authorization: Bearer <insert-jwt-token>
Content-Type: application/json

{
  "items": [
    {"productId": 1, "quantity": 2, "unitPrice": 500}
  ],
  "gstRate": 18
}
```

---

## Next Steps

1. ✅ Complete this setup guide
2. ✅ Verify both frontend and backend are running
3. ✅ Read [ARCHITECTURE_GUIDE.md](ARCHITECTURE_GUIDE.md)
4. ✅ Review [API_REFERENCE.md](API_REFERENCE.md)
5. ✅ Check out your assigned feature
6. ✅ Read feature-specific documentation in `src/features/{feature}/`
7. ✅ Start development!

---

## Getting Help

| Issue | Resource |
|-------|----------|
| Setup problem | Read Troubleshooting section |
| Code question | Check feature documentation |
| API question | See API_REFERENCE.md |
| Architecture | Read ARCHITECTURE_GUIDE.md |
| Git issue | Use git commands reference |

---

**Happy Coding!** 🚀

For questions, ask in #dev-support channel or create an issue on GitHub.

**Last Updated**: March 14, 2026
