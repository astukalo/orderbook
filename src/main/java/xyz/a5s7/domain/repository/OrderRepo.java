package xyz.a5s7.domain.repository;

import xyz.a5s7.domain.model.LimitOrder;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class OrderRepo {
    private final Map<Long, LimitOrder> orders = new HashMap<>();
    private final AtomicLong id = new AtomicLong(1);

    public LimitOrder save(final LimitOrder order) {
        LimitOrder orderToSave = order;
        if (order.getId() == null) {
            orderToSave = new LimitOrder(generateId(), order.getUserId(), order.getTicker(), order.getDirection(),
                    order.getPrice(), order.getQuantity(), order.getTimestamp());
        }
        orders.put(orderToSave.getId(), orderToSave);
        return orderToSave;
    }
    private long generateId() {
        return id.getAndIncrement();
    }

    public Optional<LimitOrder> findById(Long orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }
}
