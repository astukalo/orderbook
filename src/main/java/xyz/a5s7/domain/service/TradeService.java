package xyz.a5s7.domain.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import xyz.a5s7.domain.model.Trade;
import xyz.a5s7.domain.model.TradeListener;
import xyz.a5s7.domain.repository.TradeRepo;

@Service
public class TradeService implements TradeListener {
    private final Logger log = LoggerFactory.getLogger(TradeService.class);
    private final TradeRepo tradeRepo;

    public TradeService(TradeRepo tradeRepo) {
        this.tradeRepo = tradeRepo;
    }

    public Trade save(Trade trade) {
        return tradeRepo.save(trade);
    }

    public List<Trade> findTrades(Long orderId) {
        return tradeRepo.findTradesByOrderId(orderId)
                .orElseGet(List::of);
    }

    @Override
    @Async
    public void onTrade(Trade trade) {
        var persistedTrade = save(trade);
        log.info("Trade saved: {}", persistedTrade);
    }
}
