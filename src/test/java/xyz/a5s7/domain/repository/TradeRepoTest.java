package xyz.a5s7.domain.repository;

import xyz.a5s7.domain.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TradeRepoTest {

    private TradeRepo tradeRepo;

    @BeforeEach
    void setUp() {
        tradeRepo = new TradeRepo();
    }

    @Test
    @DisplayName("should save trade and assign id if not present")
    void shouldSaveTradeAndAssignIdIfNotPresent() {
        Trade trade = new Trade(null, 1L, 2L, new BigDecimal("43251.00"), new BigDecimal("1.0"), ZonedDateTime.now());
        Trade savedTrade = tradeRepo.save(trade);

        assertThat(savedTrade).usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(new Trade(1L, 1L, 2L, new BigDecimal("43251.00"), new BigDecimal("1.0"), null));
    }

    @Test
    @DisplayName("should save trade with existing id")
    void shouldSaveTradeWithExistingId() {
        Trade trade = new Trade(1L, 1L, 2L, new BigDecimal("43251.00"), new BigDecimal("1.0"), ZonedDateTime.now());
        Trade savedTrade = tradeRepo.save(trade);

        assertThat(savedTrade).isEqualTo(trade);
    }

    @Test
    @DisplayName("should find trades by order id")
    void shouldFindTradesByOrderId() {
        Trade trade1 = new Trade(null, 1L, 2L, new BigDecimal("43251.00"), new BigDecimal("1.0"), ZonedDateTime.now());
        Trade trade2 = new Trade(null, 2L, 1L, new BigDecimal("43251.00"), new BigDecimal("1.0"), ZonedDateTime.now());
        tradeRepo.save(trade1);
        tradeRepo.save(trade2);

        Optional<List<Trade>> tradesForOrder1 = tradeRepo.findTradesByOrderId(1L);
        Optional<List<Trade>> tradesForOrder2 = tradeRepo.findTradesByOrderId(1L);

        assertThat(tradesForOrder1).isPresent();
        assertThat(tradesForOrder1.get()).hasSize(2);
        assertThat(tradesForOrder2).isPresent();
        assertThat(tradesForOrder2.get()).containsExactlyInAnyOrder(tradesForOrder1.get().toArray(new Trade[2]));

    }

    @Test
    @DisplayName("should return empty if no trades found for order id")
    void shouldReturnEmptyIfNoTradesFoundForOrderId() {
        Optional<List<Trade>> trades = tradeRepo.findTradesByOrderId(999L);

        assertThat(trades).isNotPresent();
    }
}