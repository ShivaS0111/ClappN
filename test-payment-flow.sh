#!/bin/bash
# Payment Flow Test Script
# Usage: bash test-payment-flow.sh [order_id] [amount] [currency] [gateway]

API_BASE_URL="http://localhost:9090/api/payments"
ORDER_ID=${1:-100}
AMOUNT=${2:-5000}
CURRENCY=${3:-INR}
GATEWAY=${4:-RAZORPAY}

echo "=========================================="
echo "Payment Flow Test Script"
echo "=========================================="
echo "API Base URL: $API_BASE_URL"
echo "Order ID: $ORDER_ID"
echo "Amount: $AMOUNT (in minor units)"
echo "Currency: $CURRENCY"
echo "Gateway: $GATEWAY"
echo ""

# Step 1: Initiate Payment
echo "Step 1: Initiating Payment..."
echo "Request:"
echo "POST $API_BASE_URL/initiate"
echo "Body: {\"orderId\":$ORDER_ID,\"amount\":$AMOUNT,\"currency\":\"$CURRENCY\",\"gateway\":\"$GATEWAY\"}"
echo ""

RESPONSE=$(curl -s -X POST "$API_BASE_URL/initiate" \
  -H "Content-Type: application/json" \
  -d "{\"orderId\":$ORDER_ID,\"amount\":$AMOUNT,\"currency\":\"$CURRENCY\",\"gateway\":\"$GATEWAY\"}")

echo "Response:"
echo "$RESPONSE" | jq '.'
echo ""

# Extract payment ID
PAYMENT_ID=$(echo "$RESPONSE" | jq -r '.paymentId')
REDIRECT_URL=$(echo "$RESPONSE" | jq -r '.redirectUrl')

if [ "$PAYMENT_ID" == "null" ] || [ -z "$PAYMENT_ID" ]; then
  echo "❌ Error: Failed to initiate payment"
  exit 1
fi

echo "✓ Payment initiated successfully!"
echo "Payment ID: $PAYMENT_ID"
echo ""

# Step 2: Check Payment Status
echo "Step 2: Checking Payment Status..."
echo "Request:"
echo "GET $API_BASE_URL/status/$PAYMENT_ID"
echo ""

STATUS_RESPONSE=$(curl -s -X GET "$API_BASE_URL/status/$PAYMENT_ID" \
  -H "Content-Type: application/json")

echo "Response:"
echo "$STATUS_RESPONSE" | jq '.'
echo ""

# Extract status
PAYMENT_STATUS=$(echo "$STATUS_RESPONSE" | jq -r '.status')
echo "Payment Status: $PAYMENT_STATUS"
echo ""

# Step 3: Generate Payment Link
echo "Step 3: Payment Link Generated"
echo "Open the following URL in your browser to complete payment:"
echo "http://localhost:9090$REDIRECT_URL"
echo ""

echo "=========================================="
echo "✓ Payment Flow Test Complete"
echo "=========================================="

