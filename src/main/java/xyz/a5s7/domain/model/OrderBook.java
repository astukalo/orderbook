package xyz.a5s7.domain.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Queue;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an order book for a specific ticker symbol.
 */
public class OrderBook {
    private static final Logger log = LoggerFactory.getLogger(OrderBook.class);

    private final String ticker;
    private final NavigableMap<BigDecimal, Queue<LimitOrder>> bids = new TreeMap<>();
    private final NavigableMap<BigDecimal, Queue<LimitOrder>> asks = new TreeMap<>();
    private final List<TradeListener> onTradeListeners;

    public OrderBook(String ticker, List<TradeListener> onTradeListeners) {
        this.ticker = ticker;
        this.onTradeListeners = onTradeListeners;
    }

    public void addOrder(LimitOrder order) {
        Objects.requireNonNull(order);
        if (order.getId() == null) {
            throw new IllegalArgumentException("Order id is required");
        }

        NavigableMap<BigDecimal, Queue<LimitOrder>> oppositeSideOrders;
        NavigableMap<BigDecimal, Queue<LimitOrder>> restingOrdersMap = Collections.emptyNavigableMap();
        NavigableMap<BigDecimal, Queue<LimitOrder>> orders = Collections.emptyNavigableMap();
        switch (order.getDirection()) {
            case ASK -> {
                orders = asks;
                oppositeSideOrders = bids;
                restingOrdersMap = oppositeSideOrders.tailMap(order.getPrice(), true).descendingMap();
            }
            case BID -> {
                orders = bids;
                oppositeSideOrders = asks;
                restingOrdersMap = oppositeSideOrders.headMap(order.getPrice(), true);
            }
        }
        Long aggressingOrderId = order.getId();
        //Orders are first matched in order of price (most aggressive to least aggressive)
        for (Iterator<Queue<LimitOrder>> iterator = restingOrdersMap.values().iterator(); iterator.hasNext(); ) {
            var restingOrders = iterator.next();
            // then by arrival time into the book (oldest to newest)
            while (!restingOrders.isEmpty() && order.getPendingQuantity().compareTo(BigDecimal.ZERO) > 0) {
                LimitOrder restingOrder = restingOrders.peek();
                if (restingOrder != null) {
                    //TODO do not allow self trade - when user places an order that matches with their own order
                    var tradeQuantity = order.getPendingQuantity().min(restingOrder.getPendingQuantity());
                    restingOrder.reducePendingQuantity(tradeQuantity);
                    order.reducePendingQuantity(tradeQuantity);

                    var trade = new Trade(aggressingOrderId, restingOrder.getId(), 
                        restingOrder.getPrice(), tradeQuantity, ZonedDateTime.now()
                    );
                    log.info("Order matched, trade: {}", trade);
                    onTradeListeners.forEach(listener -> {
                        try {
                            listener.onTrade(trade);
                        } catch (Throwable e) {
                            log.error("Error processing trade", e);
                        }
                    });

                    if (restingOrder.getPendingQuantity().compareTo(BigDecimal.ZERO) == 0) {
                        restingOrders.poll();
                    }
                }
            }
            if (restingOrders.isEmpty()) {
                iterator.remove();
            }
        }
        if (order.getPendingQuantity().compareTo(BigDecimal.ZERO) > 0) {
            orders.computeIfAbsent(order.getPrice(), k -> new LinkedList<>()).add(order);
        }
    }

    NavigableMap<BigDecimal, Queue<LimitOrder>> getAsksMap() {
        return Collections.unmodifiableNavigableMap(asks);
    }

    NavigableMap<BigDecimal, Queue<LimitOrder>> getBidsMap() {
        return Collections.unmodifiableNavigableMap(bids);
    }

    @Override
    public String toString() {
        return "OrderBook{" +
                "ticker='" + ticker + '\'' +
                ", bids=" + bids +
                ", asks=" + asks +
                '}';
    }
}
