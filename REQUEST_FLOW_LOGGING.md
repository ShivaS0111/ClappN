# Request Flow Logging Guide

This document describes the complete request flow from entry to controller with detailed logging at each stage.

## How to View Logs

Set your application logging level to TRACE to see the detailed flow:

```properties
# application.properties
logging.level.biz.craftline.server.config.security=TRACE
logging.level.biz.craftline.server.config.observability=TRACE
logging.level.biz.craftline.server.feature.usermanagement.api.controller=TRACE
```

Or via environment variable:
```bash
export LOGGING_LEVEL_BIZ_CRAFTLINE_SERVER_CONFIG_SECURITY=TRACE
```

## Request Flow Sequence

When a request arrives with `Authorization: Bearer <token>`, here's the complete flow:

### Stage 1: Rate Limit Filter
**Class:** `AuthRateLimitFilter` (HIGHEST_PRECEDENCE + 20)

```
[1-AuthRateLimitFilter] ENTER: method=GET path=/api/me/context ip=127.0.0.1 enabled=true
[1-AuthRateLimitFilter] PASS: count=1/30
```

**What it does:**
- Checks if request is on a limited path (login, register, etc.)
- Tracks requests per IP + path in a 60-second window
- Blocks with 429 if limit exceeded

---

### Stage 2: Observability Filter
**Class:** `HttpRequestObservabilityFilter` (HIGHEST_PRECEDENCE + 10)

```
[2-HttpRequestObservabilityFilter] ENTER: method=GET path=/api/me/context
```

**What it does:**
- Starts request latency timer
- Prepares metrics collection
- Logs slow requests (> 2 seconds)

---

### Stage 3: JWT Authentication Filter
**Class:** `JwtAuthenticationFilter`

```
[3-JwtAuthenticationFilter] ENTER: path=/api/me/context
[3-JwtAuthenticationFilter] Token found, validating...
[3-JwtTokenProvider] validateToken: Parsing token signature...
[3-JwtTokenProvider] validateToken: SUCCESS - Token signature valid
[3-JwtAuthenticationFilter] Token valid for user: admin@clapp.com
[3-JwtAuthenticationFilter] SET: Authentication context set for admin@clapp.com
```

**What it does:**
- Extracts Bearer token from `Authorization` header
- Validates JWT signature using app.jwt.secret
- Extracts username (email) from token subject
- Sets authentication in `SecurityContextHolder` with identity only (no permissions yet)

**Common Errors:**
- `No Bearer token found` → Token not in header or wrong format
- `Token validation FAILED` → Invalid signature, expired, or wrong secret
- `Parsing token signature...` → JWT parsing exception during validation

---

### Stage 4: User Scope Filter
**Class:** `UserScopeFilter` (LOWEST_PRECEDENCE - 50)

```
[4-UserScopeFilter] ENTER: path=/api/me/context
[4-UserScopeFilter] RESOLVE: email=admin@clapp.com storeId=null businessId=null
[4-UserScopeResolver] ENTER: resolve email=admin@clapp.com storeId=null businessId=null
[4-UserScopeResolver] FOUND: userId=2 user_roles=[SYSTEM_ADMIN]
[4-UserScopeResolver] MEMBERSHIPS: count=0
[4-UserScopeResolver] UNRESTRICTED: true
[4-UserScopeResolver] RETURN: unrestricted=true roles=[SYSTEM_ADMIN] perms_count=188 stores=null businesses=null
[4-UserScopeFilter] RESOLVED: roles=[SYSTEM_ADMIN] unrestricted=true perms_count=188
[4-UserScopeFilter] SET: ScopedAuthenticationToken created
[4-UserScopeFilter] EXIT: Context cleared
```

**What it does:**
- Queries database for user by email
- Loads active memberships
- Builds permission set from roles + allowed/denied permissions
- Resolves accessible stores and businesses
- Creates `ScopedAuthenticationToken` with full authorities
- Stores context in ThreadLocal (`UserScopeContextHolder`)
- Clears ThreadLocal in finally block

**Common Errors:**
- `User not found in database: admin@clapp.com` → User exists in JWT but not in DB
- `No memberships for business: X` → User doesn't have access to requested business
- `No authenticated user in context` → Earlier filters didn't set authentication

---

### Stage 5: Spring Security Authorization Check
**Class:** Spring Security `FilterChainProxy`

```
Authorization check: Is the request authenticated?
✓ YES → Continue to interceptor
✗ NO → Return 401 Unauthorized
```

**What it does:**
- Enforces `.anyRequest().authenticated()`
- Checks if `SecurityContextHolder` has authenticated principal

---

### Stage 6: Permission Interceptor
**Class:** `RequirePermissionInterceptor`

```
[5-RequirePermissionInterceptor] ENTER: path=/api/me/context handler=MeContextController
[5-RequirePermissionInterceptor] SKIP: No @RequirePermission annotation on getContext
```

**What it does:**
- Checks for `@RequirePermission` annotation on controller method
- If present, validates user has the required permission
- Returns 403 BEFORE Bean Validation if permission check fails

**Why it's an interceptor, not AOP:**
- Runs BEFORE `@Valid` annotation processing
- Ensures 403 (not 400) for unauthorized callers with invalid bodies

---

### Stage 7: Argument Resolution
**Class:** Spring MVC `ArgumentResolvers`

```
- Path parameter binding
- Query parameter binding
- Request header binding
- @Valid Bean Validation
```

---

### Stage 8: Controller Execution
**Class:** `MeContextController`

```
[6-MeContextController] ENTER: /api/me/context
[6-MeContextController] SCOPE: userId=2 email=admin@clapp.com roles=[SYSTEM_ADMIN] unrestricted=true
[6-MeContextController] RETURN: Success
```

**What it does:**
- Retrieves `UserScopeContext` from ThreadLocal
- Accesses full user context (permissions, roles, stores, businesses)
- Builds response with user details

---

### Stage 9: Response & Observability
**Class:** `HttpRequestObservabilityFilter`

```
[2-HttpRequestObservabilityFilter] EXIT: method=GET path=/api/{id} status=200 durationMs=45 authenticated=true
```

**What it does:**
- Records request latency metric
- Logs slow request warning if > 2 seconds
- Returns response to client

---

## Troubleshooting: Unauthorized Error

If you get `"Unauthorized: authentication required"` (401), check logs in this order:

### 1. Token Reached? 
```
[3-JwtAuthenticationFilter] ENTER: path=/api/me/context
[3-JwtAuthenticationFilter] Token found, validating...
```
- ✗ **NOT FOUND** → Token not in `Authorization: Bearer ...` header
  - Check Swagger UI is sending token in request
  - Verify `persist-authorization=true` is working
  
### 2. Token Valid?
```
[3-JwtTokenProvider] validateToken: SUCCESS - Token signature valid
```
- ✗ **FAILED** → Invalid signature, expired, or wrong secret
  - Compare `app.jwt.secret` value between login and current request
  - Check token expiry time in decoded JWT
  
### 3. User in DB?
```
[4-UserScopeResolver] FOUND: userId=2 user_roles=[SYSTEM_ADMIN]
```
- ✗ **ERROR: User not found** → User exists in JWT but not in database
  - Verify user record exists: `SELECT * FROM user WHERE email = 'admin@clapp.com'`
  
### 4. User Role Set?
```
[4-UserScopeResolver] UNRESTRICTED: true
```
- ✗ **false** → User exists but has no SYSTEM_ADMIN role
  - Check user_role table for SYSTEM_ADMIN assignment
  
### 5. Scope Resolution?
```
[4-UserScopeFilter] RESOLVED: roles=[SYSTEM_ADMIN] unrestricted=true perms_count=188
```
- ✗ **ERROR** → Database lookup failed (connection, missing data, etc.)
  - Check database connectivity
  - Verify user has required membership records

---

## Log Levels Summary

| Component | Level | Purpose |
|-----------|-------|---------|
| AuthRateLimitFilter | TRACE | Rate limit checks |
| HttpRequestObservabilityFilter | TRACE | Request timing & metrics |
| JwtAuthenticationFilter | TRACE | Token extraction & validation |
| JwtTokenProvider | TRACE | JWT signature validation |
| UserScopeFilter | TRACE | Scope resolution & context setup |
| UserScopeResolver | TRACE | Database permission loading |
| RequirePermissionInterceptor | TRACE | Permission enforcement |
| MeContextController | TRACE | Controller execution |

---

## Example: Full Request Log

```
[1-AuthRateLimitFilter] ENTER: method=GET path=/api/me/context ip=127.0.0.1 enabled=true
[1-AuthRateLimitFilter] PASS: count=1/30
[2-HttpRequestObservabilityFilter] ENTER: method=GET path=/api/me/context
[3-JwtAuthenticationFilter] ENTER: path=/api/me/context
[3-JwtAuthenticationFilter] Token found, validating...
[3-JwtTokenProvider] validateToken: Parsing token signature...
[3-JwtTokenProvider] validateToken: SUCCESS - Token signature valid
[3-JwtAuthenticationFilter] Token valid for user: admin@clapp.com
[3-JwtAuthenticationFilter] SET: Authentication context set for admin@clapp.com
[4-UserScopeFilter] ENTER: path=/api/me/context
[4-UserScopeFilter] RESOLVE: email=admin@clapp.com storeId=null businessId=null
[4-UserScopeResolver] ENTER: resolve email=admin@clapp.com storeId=null businessId=null
[4-UserScopeResolver] FOUND: userId=2 user_roles=[SYSTEM_ADMIN]
[4-UserScopeResolver] MEMBERSHIPS: count=0
[4-UserScopeResolver] UNRESTRICTED: true
[4-UserScopeResolver] RETURN: unrestricted=true roles=[SYSTEM_ADMIN] perms_count=188 stores=null businesses=null
[4-UserScopeFilter] RESOLVED: roles=[SYSTEM_ADMIN] unrestricted=true perms_count=188
[4-UserScopeFilter] SET: ScopedAuthenticationToken created
[5-RequirePermissionInterceptor] ENTER: path=/api/me/context handler=MeContextController
[5-RequirePermissionInterceptor] SKIP: No @RequirePermission annotation on getContext
[6-MeContextController] ENTER: /api/me/context
[6-MeContextController] SCOPE: userId=2 email=admin@clapp.com roles=[SYSTEM_ADMIN] unrestricted=true
[6-MeContextController] RETURN: Success
[2-HttpRequestObservabilityFilter] EXIT: method=GET path=/api/{id} status=200 durationMs=45 authenticated=true
[4-UserScopeFilter] EXIT: Context cleared
```

---

## Testing Steps

1. **Set log level to TRACE:**
   ```bash
   export LOGGING_LEVEL_BIZ_CRAFTLINE_SERVER_CONFIG_SECURITY=TRACE
   export LOGGING_LEVEL_BIZ_CRAFTLINE_SERVER_CONFIG_OBSERVABILITY=TRACE
   ```

2. **Make request with token:**
   ```bash
   curl -H "Authorization: Bearer <your_token>" http://localhost:8080/api/me/context
   ```

3. **Check logs:**
   ```bash
   # View all stages in order
   grep -E "\[1-|\[2-|\[3-|\[4-|\[5-|\[6-" app.log
   ```

4. **Identify where flow stops:**
   - If no `[3-JwtAuthenticationFilter]` → Token not being sent
   - If `Token validation FAILED` → JWT validation failed
   - If `User not found` → Database issue
   - If `DENIED` → Permission check failed
