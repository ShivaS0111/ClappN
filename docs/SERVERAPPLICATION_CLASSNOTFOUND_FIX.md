# ServerApplication ClassNotFoundException - Resolution Guide

## Problem
```
Error: Could not find or load main class biz.craftline.server.ServerApplication
Caused by: java.lang.ClassNotFoundException: biz.craftline.server.ServerApplication
```

## Root Cause
The project has not been compiled yet. The compiled `.class` files don't exist in the `target/classes` directory.

---

## Solution: Build the Project

### Step 1: Clean Build
```bash
cd D:\project\ClappN
.\mvnw clean package -DskipTests
```

This will:
1. ✅ Clean old build artifacts
2. ✅ Download dependencies
3. ✅ Compile all Java files (including ServerApplication.java)
4. ✅ Run DataSeedConfig seed logic
5. ✅ Package into a JAR file

### Step 2: Wait for Build Completion
The build will take 2-5 minutes on first run.

You'll see output like:
```
[INFO] Compiling...
[INFO] Building jar...
[INFO] BUILD SUCCESS
```

### Step 3: Verify JAR Was Created
```bash
ls target/*.jar
```

Should show:
```
target/server-0.0.1-SNAPSHOT.jar
```

### Step 4: Run the Application
```bash
java -jar target/server-0.0.1-SNAPSHOT.jar
```

---

## What Happens on Startup

1. **Spring Boot Initializes**
   ```
   Started ServerApplication in X.XXX seconds
   ```

2. **DataSeedConfig Seed Bean Executes**
   ```
   [INFO] Starting database seed initialization...
   [INFO] Starting database seed...
   [INFO] Seeding Business Types...
   [INFO] Successfully seeded 85 business types
   [INFO] Seeding Categories...
   [INFO] Successfully seeded categories
   [INFO] Seeding Permissions...
   [INFO] Successfully seeded 42 permissions
   [INFO] Seeding Roles...
   [INFO] Successfully seeded 35 roles
   [INFO] Seeding Business and Store entities...
   [INFO] Successfully seeded 1 business entities
   [INFO] Successfully seeded 1 store entities
   [INFO] Database seed completed successfully!
   ```

3. **Application Ready**
   ```
   Application 'server' started on port 8080 with profile(s) [dev]
   ```

---

## Troubleshooting

### Issue: Build Fails With Errors
**Solution**: Check the error messages. Common fixes:
- Ensure MySQL is running
- Check database connection properties in `application-dev.properties`
- Verify all dependencies are installed

### Issue: Database Connection Error
**Solution**: Update `application-dev.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/clapp
spring.datasource.username=root
spring.datasource.password=your_password
```

### Issue: Seed Data Not Appearing
**Solution**: Check logs. If "Database already populated. Skipping seed.", the data is already there from a previous run.

---

## File Structure After Build

```
target/
├── classes/
│   ├── biz/
│   │   └── craftline/
│   │       └── server/
│   │           ├── ServerApplication.class ✅
│   │           ├── config/
│   │           │   └── DataSeedConfig.class ✅
│   │           ├── feature/
│   │           └── ...
│   └── application.properties
├── server-0.0.1-SNAPSHOT.jar ✅ (Executable JAR)
└── ...
```

---

## Database After Seeding

After first run, your database will have:
- ✅ 85+ business types
- ✅ 100+ categories
- ✅ 40+ permissions
- ✅ 35+ roles
- ✅ 1 sample business with gallery (4 images) & banner
- ✅ 1 sample store with gallery (5 images) & banner

---

## Status Check

| Step | Status |
|------|--------|
| Maven Compiler Updated | ✅ Done |
| DataSeedConfig Rewritten | ✅ Done |
| Project Build | 🔄 In Progress |
| JAR Created | ⏳ Pending |
| Application Started | ⏳ Pending |
| Database Seeded | ⏳ Pending |

---

**Next Steps**:
1. Wait for build to complete (2-5 minutes)
2. Verify `target/server-0.0.1-SNAPSHOT.jar` exists
3. Run: `java -jar target/server-0.0.1-SNAPSHOT.jar`
4. Check logs for seed completion
5. Query database to verify data

---

**Build Status**: Currently compiling all Java files...

