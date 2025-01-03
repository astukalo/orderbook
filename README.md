# Order Book Matching Engine

## Introduction
A matching engine is a technology that lies at the core of any exchange.
From a high-level perspective matching engine matches people (or organizations)
who want to buy an asset with people who want to sell an asset.
We want to support only one type of order for this exercise, namely limit order.
A limit order is an order to buy an asset at no more than a specific price
or sell an asset at no less than a specific price.

In real life, different algorithms can be used to match orders.
Here a continuous trading Price/Time algorithm (aka FIFO) is implemented.
This algorithm ensures that all orders at the same price level are filled according to time
priority; the first order at a price level is the first order matched. For any new order,
the opposite order with the best price is prioritized, and if there are multiple orders
with the same price, the earliest takes precedence.

Usually, the term order book is used to list all active buy (BID) and sell (ASK) orders.
For example, consider such order book:

| ID | Direction | Time  | Amount | Price | Amount | Time  | Direction |
|----|-----------|-------|-------:|-------|-------:|-------|-----------|
| 3  |           |       |        | 10.05 | 40     | 07:03 | ASK      |
| 1  |           |       |        | 10.05 | 20     | 07:00 | ASK      |
| 2  |           |       |        | 10.04 | 20     | 07:02 | ASK      |
| 5  | BID       | 07:06 | 40     | 10.02 |        |       |           |
| 4  | BID       | 07:05 | 20     | 10.00 |        |       |           |
| 6  | BID       | 07:10 | 40     | 10.00 |        |       |           |

If a new limit order `buy 55 shares at 10.06 price` comes in,
then it will be filled in this order:
1. 20 shares at 10.04 (order 2)
2. 20 shares at 10.05 (order 1)
3. 15 shares at 10.05 (order 3)

This leaves the order book in the following state:

| ID | Direction | Time  | Amount | Price | Amount | Time  | Direction |
|----|-----------|-------|-------:|-------|-------:|-------|-----------|
| 3  |           |       |        | 10.05 | 25     | 07:03 | SELL      |
| 5  | BUY       | 07:06 | 40     | 10.02 |        |       |           |
| 4  | BUY       | 07:05 | 20     | 10.00 |        |       |           |
| 6  | BUY       | 07:10 | 40     | 10.00 |        |       |           |

NB: order 3 is executed only partially.

## About the project
The project implements a simple order book matching engine.
See [OrderBook](src/main/java/xyz/a5s7/domain/model/OrderBook.java) class for the implementation details.

It provides a REST API to place orders and get the current state of the order book and trades.

## API
### Place Order
- **Endpoint**: `POST /orders`
- **Description**: Places a new limit order in the order book.
- **Headers**:
  - `Authorization`: User ID (used to identify the user placing the order)
- **Request Body**:
  ```json
  {
    "ticker": "BTC",
    "price": 90000.00,
    "quantity": 0.35,
    "direction": "BID"
  }
- **Response Body**:
  ```json
  {
    "id": 1,
    "timestamp": "2025-01-03T15:16:11.886684+01:00",
    "ticker": "BTC",
    "price": 90000.00,
    "quantity": 0.35,
    "direction": "BID",
    "pendingQuantity": 0.35
  }
  ```

### Get current order state
- **Endpoint**: `GET /orders/{orderId}`
- **Description**: Responds with the current state of the order with ID `orderId`.
- **Headers**:
  - `Authorization`: User ID (used to identify the user placing the order)
- **Response Body**:
  ```json
  {
    "id": 1,
    "timestamp": "2025-01-03T15:16:11.886684+01:00",
    "ticker": "BTC",
    "price": 90000.00,
    "quantity": 0.35,
    "direction": "BID",
    "pendingQuantity": 0.35
  }
  ```

### Get trades for order
- **Endpoint**: `GET /trades?orderId={orderId}`
- **Description**: Responds with all trades that have been executed for the order with ID `orderId`.
- **Headers**:
- `Authorization`: User ID (used to identify the user placing the order)
- **Response Body**:
  ```json
  [
    {
      "quantity": 0.35,
      "price": 93251.00,
      "executedAt": "2025-01-03T15:16:11.997206+01:00"
    },
    {
      "quantity": 0.65,
      "price": 93251.00,
      "executedAt": "2025-01-03T15:16:12.235238+01:00"
    }
  ]
  ```

## Examples
See [client.http](client.http) for examples of API usage.

