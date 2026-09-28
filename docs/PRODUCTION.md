# Production configuration

Canonical module status: [BACKEND_STATUS.md](./BACKEND_STATUS.md).  
Env template: [`../.env.example`](../.env.example).

## 1. Payment keys (Stripe / Razorpay)

Set **environment variables** (not hardcoded in git). Prod profile maps them in `application-prod.properties`.

| Provider | Env vars |
|----------|----------|
| Stripe | `STRIPE_SECRET`, `STRIPE_PUBLISHABLE`, `STRIPE_WEBHOOK_SECRET`, `PAYMENT_STRIPE_SUCCESS_URL`, `PAYMENT_STRIPE_CANCEL_URL` |
| Razorpay | `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`, `RAZORPAY_WEBHOOK_SECRET` |
| Which to enable | `APP_PAYMENT_PROVIDERS=stripe` or `razorpay` or `stripe,razorpay` |

**Webhook endpoints to register at the provider dashboard:**

- Stripe → `POST https://<api-host>/api/payments/webhook/stripe`
- Razorpay → `POST https://<api-host>/api/payments/webhook/razorpay`

On `prod`, `PaymentStartupValidator` **refuses to start** if an enabled provider still has placeholder/missing secrets.

**Local / staging with real test keys** (not prod profile):

```text
SPRING_PROFILES_ACTIVE=dev
STRIPE_SECRET=sk_test_...
STRIPE_PUBLISHABLE=pk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
RAZORPAY_KEY_ID=rzp_test_...
RAZORPAY_KEY_SECRET=...
RAZORPAY_WEBHOOK_SECRET=...
APP_PAYMENTS_REQUIRE_WEBHOOK_VERIFICATION=true   # optional: enforce real secrets even on dev
```

## 2. SMTP mail

| Env | Purpose |
|-----|---------|
| `APP_MAIL_MODE=smtp` | Use real SMTP (prod default) |
| `APP_MAIL_FROM` | From address |
| `SPRING_MAIL_HOST` / `PORT` / `USERNAME` / `PASSWORD` | SMTP server |
| `APP_RESET_PASSWORD_URL` | Link in reset emails |

Dev default is `APP_MAIL_MODE=log` (prints to logs).

## 3. Other required prod env

| Env | Purpose |
|-----|---------|
| `SPRING_DATASOURCE_URL` / `USERNAME` / `PASSWORD` | MySQL |
| `JWT_SECRET` | Strong secret (required; no default in prod) |
| `APP_CORS_ORIGINS` | Comma-separated frontend origins |

## 4. Auth hardening (built-in)

| Feature | Behavior |
|---------|----------|
| Rate limit | `AuthRateLimitFilter` on login/register/forgot/reset/refresh (429 when exceeded) |
| Logout | Revokes **all** refresh tokens for that user |
| Prod SMTP check | Fails start if `APP_MAIL_MODE=smtp` but `SPRING_MAIL_HOST` empty |

Tune: `APP_RATE_LIMIT_AUTH_MAX` (prod default 15 / 60s), `APP_RATE_LIMIT_ENABLED`.

## 5. Start checklist

1. MySQL up; Flyway will apply through **V18**
2. Copy `.env.example` → set real values (never commit `.env`)
3. `SPRING_PROFILES_ACTIVE=prod`
4. Start app; confirm logs: `Payment secret checks passed` and `Production secret checks passed`
5. Smoke: login → place order → initiate payment → webhook confirm → invoice PDF/email

## 5. Out of scope for this production bar

Coupons, deep tax engine, marketing modules — product features, not ops blockers for core POS.
