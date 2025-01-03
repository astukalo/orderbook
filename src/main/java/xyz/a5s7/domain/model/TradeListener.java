package xyz.a5s7.domain.model;

@FunctionalInterface
public interface TradeListener {
    void onTrade(Trade trade);
} 