# Payment Flow - Quick Reference

## Architecture Overview
```
User Order → Payment Page → Payment Gateway → Webhook → Order Confirmation
```

## API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/payments/initiate` | Start payment process |
| GET | `/api/payments/status/{paymentId}` | Check payment status |
| POST | `/api/payments/webhook/stripe` | Stripe webhook handler |
| POST | `/api/payments/webhook/razorpay` | Razorpay webhook handler |

## Request/Response Examples

### Initiate Payment
```bash
curl -X POST http://localhost:9090/api/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 123,
    "amount": 5000,
    "currency": "INR",
    "gateway": "RAZORPAY"
  }'
```

**Response:**
```json
{
  "paymentId": "PAY_RP_1700000000000",
  "providerOrderId": "order_RP_1700000000000",
  "redirectUrl": "/pay/index.html#token=...",
  "gateway": "RAZORPAY"
}
```

### Get Payment Status
```bash
curl http://localhost:9090/api/payments/status/PAY_RP_1700000000000
```

**Response:**
```json
{
  "id": 1,
  "orderId": "123",
  "gateway": "RAZORPAY",
  "providerPaymentId": "PAY_RP_1700000000000",
  "currency": "INR",
  "amount": 5000,
  "status": "PENDING",
  "createdAt": "2024-11-16T10:30:00"
}
```

## Token Format (Base64 Encoded)

```json
{
  "orderId": 123,
  "amount": 5000,
  "currency": "INR",
  "gateway": "RAZORPAY"
}
```

**URL:** `http://localhost:9090/pay/index.html#token={BASE64_ENCODED_TOKEN}`

## Configuration

Add to `application.properties`:
```properties
payment.stripe.secret=sk_test_YOUR_SECRET
payment.stripe.publishable=pk_test_YOUR_PUBLIC
payment.razorpay.key_id=rzp_test_YOUR_KEY_ID
payment.razorpay.key_secret=YOUR_SECRET_KEY
```

## Error Messages

| Error | Cause | Solution |
|-------|-------|----------|
| `Invalid orderId` | orderId is null or <= 0 | Provide valid positive integer |
| `Invalid amount` | amount is null or <= 0 | Use minor units (paise/cents) |
| `Currency is required` | currency field missing | Provide currency code |
| `Payment gateway is required` | gateway field missing | Use STRIPE or RAZORPAY |
| `Unsupported gateway` | Gateway not recognized | Check gateway name spelling |
| `No redirect URL provided` | Backend error | Check server logs |

## Database Table

```sql
CREATE TABLE payment_transaction (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id VARCHAR(50),
  gateway VARCHAR(20),
  provider_payment_id VARCHAR(100),
  provider_order_id VARCHAR(100),
  currency VARCHAR(10),
  amount BIGINT,
  status VARCHAR(20),
  created_at DATETIME,
  updated_at DATETIME
);
```

## Payment Statuses

- **PENDING**: Payment initiated, awaiting completion
- **SUCCESS**: Payment completed successfully
- **FAILED**: Payment failed or cancelled
- **REFUNDED**: Payment refunded

## Testing

### 1. Import Postman Collection
Import: `postman/Payment_Flow_Testing.postman_collection.json`

### 2. Run Tests
```bash
# Test Razorpay payment
curl -X POST http://localhost:9090/api/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 100,
    "amount": 5000,
    "currency": "INR",
    "gateway": "RAZORPAY"
  }'
```

### 3. Check Status
```bash
# Replace PAY_ID with actual payment ID from response
curl http://localhost:9090/api/payments/status/PAY_ID
```

## Integration Steps

### 1. Generate Token in Your Backend
```java
String token = "{\"orderId\":123,\"amount\":5000,\"currency\":\"INR\",\"gateway\":\"RAZORPAY\"}";
String encoded = Base64.getUrlEncoder().encodeToString(token.getBytes());
String paymentUrl = "/pay/index.html#token=" + encoded;
```

### 2. Redirect User
```java
return "redirect:" + paymentUrl;
```

### 3. Handle Webhook
```java
@PostMapping("/webhook/razorpay")
public ResponseEntity<?> webhook(@RequestBody String payload) {
    // Verify signature
    // Update PaymentTransaction status
    // Update Order status
}
```

## Amount Format

| Currency | Amount | In Minor Units | Display |
|----------|--------|-----------------|---------|
| INR | ₹50.00 | 5000 | 50.00 |
| USD | $10.00 | 1000 | 10.00 |
| EUR | €10.00 | 1000 | 10.00 |

**Formula:** Display Amount = Minor Units / 100

## Key Files

| File | Purpose |
|------|---------|
| `PaymentController.java` | REST endpoints |
| `PaymentServiceImpl.java` | Business logic |
| `StripePaymentProvider.java` | Stripe integration |
| `RazorpayPaymentProvider.java` | Razorpay integration |
| `PaymentTransaction.java` | Database entity |
| `index.html` | Payment page |

## Logging

Enable debug logging:
```properties
logging.level.biz.craftline.server.feature.paymentmanagement=DEBUG
```

Check logs:
```bash
tail -f logs/app.log
```

## Troubleshooting

### Payment page shows error
1. Check browser console for errors
2. Verify token is valid Base64 and contains all fields
3. Check server logs for backend errors

### No redirect after payment
1. Check if webhook is configured in payment gateway
2. Verify webhook URL is publicly accessible
3. Check webhook delivery in gateway dashboard

### Payment status shows PENDING
1. Wait for webhook to arrive (usually < 1 second)
2. Check webhook configuration
3. Monitor server logs for webhook errors

## Support

- Documentation: `doc/PAYMENT_FLOW_GUIDE.md`
- Validation Report: `doc/PAYMENT_VALIDATION_REPORT.md`
- Postman Collection: `postman/Payment_Flow_Testing.postman_collection.json`
- Test Script: `test-payment-flow.sh`

---
**Last Updated:** November 16, 2024

