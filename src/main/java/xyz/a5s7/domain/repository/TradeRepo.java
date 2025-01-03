package xyz.a5s7.domain.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import xyz.a5s7.domain.model.Trade;

@Repository
public class TradeRepo {
    private final Map<Long, List<Trade>> trades = new ConcurrentHashMap<>();
    private final AtomicLong id = new AtomicLong(1);

    public Trade save(final Trade trade) {
        Trade tradeToSave = trade;
        if (trade.id() == null) {
            tradeToSave = new Trade(generateId(), trade.aggressingId(), trade.restingId(), trade.price(), trade.quantity(), trade.timestamp());
        }
        trades.computeIfAbsent(tradeToSave.restingId(), it -> new ArrayList<>()).add(tradeToSave);
        trades.computeIfAbsent(tradeToSave.aggressingId(), it -> new ArrayList<>()).add(tradeToSave);
        return tradeToSave;
    }

    private long generateId() {
        return id.getAndIncrement();
    }

    public Optional<List<Trade>> findTradesByOrderId(Long orderId) {
        return Optional.ofNullable(trades.get(orderId));
    }
}
