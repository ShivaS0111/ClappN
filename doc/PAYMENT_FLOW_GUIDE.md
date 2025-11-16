# Payment Flow Implementation Guide

## Overview
The payment flow supports two payment gateways:
1. **Stripe** - Credit/Debit cards, more payment methods
2. **Razorpay** - Indian payment gateway, UPI, cards, etc.

## Payment Flow Architecture

```
Order Placed
    ↓
GET /pay/index.html#token=BASE64_ENCODED_PAYMENT_TOKEN
    ↓
Frontend parses token (orderId, amount, currency, gateway)
    ↓
User clicks "Proceed to Pay"
    ↓
POST /api/payments/initiate (with payment details)
    ↓
Backend selects Payment Provider (Stripe/Razorpay)
    ↓
Provider creates order/session, returns redirect URL
    ↓
Backend saves PaymentTransaction in DB (PENDING status)
    ↓
Frontend redirects to payment gateway checkout
    ↓
User completes payment at gateway
    ↓
Payment Gateway sends Webhook to /api/payments/webhook/{stripe|razorpay}
    ↓
Backend verifies webhook signature
    ↓
Backend updates PaymentTransaction status (SUCCESS/FAILED)
```

## Token Format (Base64 Encoded JSON)

The payment token is Base64-encoded JSON with the following structure:

```json
{
  "orderId": 123,
  "amount": 5000,
  "currency": "INR",
  "gateway": "RAZORPAY"
}
```

**Important Notes:**
- `amount` is in minor units (paise for INR, cents for USD)
- 5000 paise = ₹50.00
- 1000 cents = $10.00

## API Endpoints

### 1. Initiate Payment
**POST** `/api/payments/initiate`

**Request Body:**
```json
{
  "orderId": 123,
  "amount": 5000,
  "currency": "INR",
  "gateway": "RAZORPAY"
}
```

**Response (Success 200):**
```json
{
  "paymentId": "PAY_RP_1700000000000",
  "providerOrderId": "order_RP_1700000000000",
  "redirectUrl": "/pay/index.html#token=eyJvcmRlcklkIjoxMjN...",
  "gateway": "RAZORPAY"
}
```

**Response (Error 400/500):**
```json
"Invalid orderId"
"Payment initiation failed: Unsupported gateway"
```

### 2. Get Payment Status
**GET** `/api/payments/status/{providerPaymentId}`

**Response (Success 200):**
```json
{
  "id": 1,
  "orderId": "123",
  "gateway": "RAZORPAY",
  "providerPaymentId": "PAY_RP_1700000000000",
  "providerOrderId": "order_RP_1700000000000",
  "currency": "INR",
  "amount": 5000,
  "status": "PENDING",
  "createdAt": "2024-11-16T10:30:00",
  "updatedAt": null
}
```

### 3. Stripe Webhook
**POST** `/api/payments/webhook/stripe`

**Headers:**
- `stripe-signature`: Webhook signature for verification

**Payload:**
Stripe event JSON (raw)

### 4. Razorpay Webhook
**POST** `/api/payments/webhook/razorpay`

**Headers:**
- `x-razorpay-signature`: Webhook signature for verification

**Payload:**
Razorpay event JSON

## Configuration

### Application Properties

```properties
# Payment Gateway Configuration
payment.stripe.secret=sk_test_YOUR_SECRET
payment.stripe.publishable=pk_test_YOUR_PUBLIC
payment.stripe.webhook_secret=whsec_TEST_SECRET

payment.razorpay.key_id=rzp_test_YOUR_KEY_ID
payment.razorpay.key_secret=YOUR_SECRET_KEY
payment.razorpay.webhook_secret=razorpay_webhook_secret
```

## Current Implementation Status

### ✅ Implemented
- [x] Payment initiation endpoint
- [x] Dual gateway support (Stripe & Razorpay)
- [x] Payment transaction persistence
- [x] Webhook endpoints for both gateways
- [x] Frontend payment page with token parsing
- [x] Error handling and validation
- [x] CORS configuration

### 🔄 To Implement (for production)
- [ ] Real Stripe API integration (add stripe-java dependency)
- [ ] Real Razorpay API integration (add razorpay-java dependency)
- [ ] Webhook signature verification (currently stubbed)
- [ ] Payment status update on webhook
- [ ] Webhook retry mechanism
- [ ] Payment reconciliation job
- [ ] Idempotency keys
- [ ] Encryption for sensitive data
- [ ] PCI compliance measures

## Testing the Payment Flow

### 1. Test with Postman

**Step 1: Initiate Payment**
```
POST http://localhost:9090/api/payments/initiate
Content-Type: application/json

{
  "orderId": 100,
  "amount": 5000,
  "currency": "INR",
  "gateway": "RAZORPAY"
}
```

**Response:**
```json
{
  "paymentId": "PAY_RP_1700000000000",
  "providerOrderId": "order_RP_1700000000000",
  "redirectUrl": "/pay/index.html#token=eyJvcmRlcklkIjoxMDAsImFtb3VudCI6NTAwMCwiY3VycmVuY3kiOiJJTlIiLCJnYXRld2F5IjoiUkFaT1JQQVkifQ==",
  "gateway": "RAZORPAY"
}
```

**Step 2: Open redirect URL**
Copy the `redirectUrl` and open in browser:
```
http://localhost:9090/pay/index.html#token=eyJvcmRlcklkIjoxMDAsImFtb3VudCI6NTAwMCwiY3VycmVuY3kiOiJJTlIiLCJnYXRld2F5IjoiUkFaT1JQQVkifQ==
```

**Step 3: Check payment status**
```
GET http://localhost:9090/api/payments/status/PAY_RP_1700000000000
```

### 2. Test via Frontend Integration

1. Update your order creation endpoint to generate payment token
2. Redirect user to payment page: `/pay/index.html#token=BASE64_ENCODED_TOKEN`
3. User clicks "Proceed to Pay"
4. Payment gateway opens (Razorpay/Stripe)
5. After payment, user is redirected (handled by gateway)

## Database Schema

### payment_transaction
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

## Error Handling

### Common Errors

1. **Invalid orderId**
   - Cause: orderId is null or <= 0
   - Fix: Ensure orderId is a positive integer

2. **Invalid amount**
   - Cause: amount is null or <= 0
   - Fix: Ensure amount is in minor units (positive integer)

3. **Currency is required**
   - Cause: currency field is missing or empty
   - Fix: Provide currency code (e.g., INR, USD)

4. **Unsupported gateway**
   - Cause: Gateway is not STRIPE or RAZORPAY
   - Fix: Use one of the supported gateways

5. **No redirect URL provided**
   - Cause: Backend didn't generate checkout URL
   - Fix: Check server logs for payment provider errors

## Security Considerations

### Current Implementation
- ✅ CORS enabled for all origins (can be restricted)
- ✅ Input validation on all endpoints
- ✅ Payment details in transaction DB
- ✅ Transaction logging

### Recommendations for Production
- 🔒 Restrict CORS origins to known domains
- 🔒 Implement JWT authentication on payment endpoints
- 🔒 Encrypt sensitive fields (payment IDs, gateway credentials)
- 🔒 Use HTTPS only
- 🔒 Validate webhook signatures cryptographically
- 🔒 Implement rate limiting
- 🔒 Monitor for suspicious payment patterns
- 🔒 Regular security audits

## Integration with Order Management

To integrate payment with orders:

1. **Order Creation Flow:**
   - Create order in database
   - Generate payment token with order ID, amount, currency
   - Encode token in Base64
   - Redirect to: `/pay/index.html#token={BASE64_TOKEN}`

2. **Payment Success Handling:**
   - Listen for webhook at `/api/payments/webhook/{gateway}`
   - Update PaymentTransaction status to SUCCESS
   - Update Order status to CONFIRMED/PAID
   - Send confirmation email
   - Trigger fulfillment

3. **Payment Failure Handling:**
   - Update PaymentTransaction status to FAILED
   - Keep Order in PENDING state
   - Allow user to retry payment
   - Send failure notification

## Troubleshooting

### Payment page shows "Invalid or missing payment token"
- Check that token is properly Base64 encoded
- Verify token contains all required fields: orderId, amount, currency, gateway
- Check browser console for parsing errors

### "No redirect URL provided" error
- Check server logs for payment provider initialization
- Verify payment gateway credentials in application.properties
- Ensure payment provider is returning valid response

### Webhook not received
- Check webhook configuration in payment gateway dashboard
- Verify webhook URL is publicly accessible
- Check firewall/security groups allow incoming webhooks
- Monitor application logs for webhook errors

## References

### Stripe Documentation
- https://stripe.com/docs/api
- https://stripe.com/docs/webhooks

### Razorpay Documentation
- https://razorpay.com/docs/
- https://razorpay.com/docs/webhooks/

## Version History
- v1.0 - Initial implementation with Stripe & Razorpay stub providers

