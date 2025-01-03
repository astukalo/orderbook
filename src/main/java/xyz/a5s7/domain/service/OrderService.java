package xyz.a5s7.domain.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import xyz.a5s7.domain.model.LimitOrder;
import xyz.a5s7.domain.model.OrderBook;
import xyz.a5s7.domain.model.TradeListener;
import xyz.a5s7.domain.repository.OrderRepo;

@Service
public class OrderService {
    private final Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
    private final OrderRepo orderRepo;
    private final List<TradeListener> onTradeListeners;

    public OrderService(OrderRepo orderRepo, List<TradeListener> onTradeListeners) {
        this.orderRepo = orderRepo;
        this.onTradeListeners = onTradeListeners;
    }

    public LimitOrder placeOrder(LimitOrder request) {
        LimitOrder order;
        OrderBook orderBook = orderBooks.computeIfAbsent(
            request.getTicker(), 
            ticker -> new OrderBook(ticker, onTradeListeners)
        );
        synchronized (orderBook) {
            order = orderRepo.save(request);
            orderBook.addOrder(order);
        }
        return order;
    }

    public Optional<LimitOrder> findOrder(Long orderId) {
        return orderRepo.findById(orderId);
    }
}
