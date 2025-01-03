package xyz.a5s7.domain.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

public class LimitOrder {
    private final Long id;
    // user id who placed the order
    private final Long userId;
    private final String ticker;
    private final Direction direction;
    private final BigDecimal price;
    private final BigDecimal quantity;
    private final ZonedDateTime timestamp;
    private BigDecimal pendingQuantity;

    public LimitOrder(Long id, Long userId, String ticker, Direction type, BigDecimal price, BigDecimal quantity, ZonedDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.ticker = ticker;
        this.direction = type;
        this.price = price;

        this.quantity = quantity;
        this.pendingQuantity = quantity;
        this.timestamp = timestamp;
    }

    public LimitOrder(Long userId, String ticker, Direction type, BigDecimal price, BigDecimal quantity, ZonedDateTime timestamp) {
        this(null, userId, ticker, type, price, quantity, timestamp);
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTicker() {
        return ticker;
    }

    public Direction getDirection() {
        return direction;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPendingQuantity() {
        return pendingQuantity;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void reducePendingQuantity(BigDecimal tradeQuantity) {
        pendingQuantity = pendingQuantity.subtract(tradeQuantity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LimitOrder that = (LimitOrder) o;
        return Objects.equals(id, that.id) && Objects.equals(userId, that.userId)
                && Objects.equals(ticker, that.ticker) && direction == that.direction
                && Objects.equals(price, that.price) && Objects.equals(quantity, that.quantity)
                && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, ticker, direction, price, quantity, timestamp);
    }

    @Override
    public String toString() {
        return "LimitOrder{" +
                "id=" + id +
                ", userId=" + userId +
                ", ticker='" + ticker + '\'' +
                ", direction=" + direction +
                ", price=" + price +
                ", quantity=" + quantity +
                ", timestamp=" + timestamp +
                ", pendingQuantity=" + pendingQuantity +
                '}';
    }
}
