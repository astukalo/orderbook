package xyz.a5s7.domain.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Represents a trade between two orders.
 */
public final class Trade {
    private final Long id;
    private final Long aggressingId;
    private final Long restingId;
    private final BigDecimal price;
    private final BigDecimal quantity;
    private final ZonedDateTime timestamp;

    /**
     * @param id           The id of the trade.
     * @param aggressingId The id of the order that initiated the trade.
     * @param restingId    The id of the order that was resting in the order book.
     * @param price        The price at which the trade occurred.
     * @param quantity     The quantity of the trade.
     * @param timestamp    The timestamp of the trade.
     */
    public Trade(Long id, Long aggressingId, Long restingId, BigDecimal price, BigDecimal quantity, ZonedDateTime timestamp) {
        this.id = id;
        this.aggressingId = aggressingId;
        this.restingId = restingId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

public Trade(Long aggressingId, Long restingId, BigDecimal price, BigDecimal quantity, ZonedDateTime timestamp) {
        this(null, aggressingId, restingId, price, quantity, timestamp);
    }

    public Long id() {
        return id;
    }

    public Long aggressingId() {
        return aggressingId;
    }

    public Long restingId() {
        return restingId;
    }

    public BigDecimal price() {
        return price;
    }

    public BigDecimal quantity() {
        return quantity;
    }

    public ZonedDateTime timestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Trade) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.aggressingId, that.aggressingId) &&
                Objects.equals(this.restingId, that.restingId) &&
                Objects.equals(this.price, that.price) &&
                Objects.equals(this.quantity, that.quantity) &&
                Objects.equals(this.timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, aggressingId, restingId, price, quantity, timestamp);
    }

    @Override
    public String toString() {
        return "Trade[" +
                "id=" + id + ", " +
                "aggressingId=" + aggressingId + ", " +
                "restingId=" + restingId + ", " +
                "price=" + price + ", " +
                "quantity=" + quantity + ", " +
                "timestamp=" + timestamp + ']';
    }
}
