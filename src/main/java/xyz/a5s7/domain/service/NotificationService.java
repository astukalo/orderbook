package xyz.a5s7.domain.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import xyz.a5s7.domain.model.LimitOrder;
import xyz.a5s7.domain.model.Trade;
import xyz.a5s7.domain.model.TradeListener;
import xyz.a5s7.domain.repository.OrderRepo;

/**
 * Sending notifications and saving trades to DB should go in one transaction.
 * It should be @Async method calling transactional method with:
 * - saving to DB
 * - sending notification to queue
 */
@Service
public class NotificationService implements TradeListener {
    private final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final OrderRepo orderRepo;

    public NotificationService(OrderRepo orderRepo) {
        this.orderRepo = orderRepo;
    }

    public void notifyAbout(Trade trade) {
        notifyOwnerOfOrderAboutTrade(trade.aggressingId(), trade);
        notifyOwnerOfOrderAboutTrade(trade.restingId(), trade);
    }

    private void notifyOwnerOfOrderAboutTrade(Long orderId, Trade trade) {
        Optional<LimitOrder> order = orderRepo.findById(orderId);
        order.ifPresent(limitOrder -> notifyUserAboutTrade(limitOrder, trade));
    }

    private void notifyUserAboutTrade(LimitOrder order, Trade trade) {
        boolean isFullyFilled = order.getPendingQuantity().compareTo(BigDecimal.ZERO) == 0;
        Long userId = order.getUserId();
        if (isFullyFilled) {
            log.info("MSG to user#{}: Order#{} {} {} {}@{} is fully filled",
                userId, 
                order.getId(), order.getDirection(), order.getTicker(), order.getQuantity(), order.getPrice()
            );
        } else {
            log.info("MSG to user#{}: Order#{} {} {} {}@{} is partially filled: {}@{}",
                userId, 
                order.getId(), order.getDirection(), order.getTicker(), order.getQuantity(), order.getPrice(),
                trade.quantity(), trade.price()
            );
        }
    }

    @Override
    @Async
    public void onTrade(Trade trade) {
        notifyAbout(trade);
    }
}
