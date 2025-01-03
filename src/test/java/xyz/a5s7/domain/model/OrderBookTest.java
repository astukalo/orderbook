package xyz.a5s7.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

public class OrderBookTest {
    private OrderBook orderBook;
    @Captor
    private ArgumentCaptor<Trade> tradeCaptor;
    private TradeListener tradeListener;

    @BeforeEach
    public void setup() {
        var tradeListeners = new ArrayList<TradeListener>();
        tradeListener = mock(TradeListener.class);
        tradeListeners.add(tradeListener);
        orderBook = new OrderBook("ticker", tradeListeners);
        tradeCaptor = ArgumentCaptor.forClass(Trade.class);
    }

    @Test
    public void shouldMatchOrdersWhenPricesAreEqual() {
        LimitOrder buyOrder = new LimitOrder(1L, 100L, "ticker", Direction.BID, new BigDecimal(100), new BigDecimal(10), null);
        LimitOrder sellOrder = new LimitOrder(2L, 200L, "ticker", Direction.ASK, new BigDecimal(100), new BigDecimal(10), null);

        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);

        assertThat(orderBook.getAsksMap()).isEmpty();
        assertThat(orderBook.getBidsMap()).isEmpty();

        verify(tradeListener).onTrade(tradeCaptor.capture());
        Trade capturedTrade = tradeCaptor.getValue();
        assertThat(capturedTrade)
                .usingRecursiveComparison()
                .ignoringFields("timestamp", "id")
                .isEqualTo(new Trade(2L, 1L, new BigDecimal(100), new BigDecimal(10), null));
    }

    @Test
    public void shouldNotMatchOrdersWhenPricesAreNotEqual() {
        LimitOrder buyOrder = new LimitOrder(1L, 100L, "ticker", Direction.BID, new BigDecimal(100), new BigDecimal(10), null);
        LimitOrder sellOrder = new LimitOrder(2L, 200L, "ticker", Direction.ASK, new BigDecimal(102), new BigDecimal(5), null);

        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);

        verify(tradeListener, never()).onTrade(any());
        assertThat(orderBook.getBidsMap()).hasSize(1);
        assertThat(orderBook.getAsksMap()).hasSize(1);
    }

    @Test
    public void shouldPartiallyMatchOrdersWhenSellQuantityIsLessThanBuyQuantity() {
        LimitOrder buyOrder = new LimitOrder(1L, 100L, "ticker", Direction.BID, new BigDecimal(100), new BigDecimal(10), null);
        LimitOrder sellOrder = new LimitOrder(2L, 200L, "ticker", Direction.ASK, new BigDecimal(100), new BigDecimal(5), null);

        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);

        assertThat(orderBook.getBidsMap()).hasSize(1);
        assertThat(orderBook.getBidsMap().get(new BigDecimal(100))).hasSize(1);
        assertThat(orderBook.getBidsMap().get(new BigDecimal(100)).peek().getPendingQuantity())
                .isEqualTo(new BigDecimal(5));
        assertThat(orderBook.getAsksMap()).isEmpty();

        verify(tradeListener).onTrade(tradeCaptor.capture());
        Trade capturedTrade = tradeCaptor.getValue();
        assertThat(capturedTrade)
                .usingRecursiveComparison()
                .ignoringFields("timestamp", "id")
                .isEqualTo(new Trade(2L, 1L, new BigDecimal(100), new BigDecimal(5), null));
    }

    @Test
    public void shouldPartiallyMatchOrdersWhenAskPriceIsLowerBestBidPrice() {
        LimitOrder buyOrder = new LimitOrder(1L, 100L, "ticker", Direction.BID, new BigDecimal(100), new BigDecimal(5), null);
        LimitOrder sellOrder = new LimitOrder(2L, 200L, "ticker", Direction.ASK, new BigDecimal(98), new BigDecimal(10), null);

        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);

        assertThat(orderBook.getBidsMap()).isEmpty();
        assertThat(orderBook.getAsksMap()).hasSize(1);
        assertThat(orderBook.getAsksMap().get(new BigDecimal(98))).hasSize(1);
        assertThat(orderBook.getAsksMap().get(new BigDecimal(98)).peek().getPendingQuantity())
                .isEqualTo(new BigDecimal(5));

        verify(tradeListener).onTrade(tradeCaptor.capture());
        Trade capturedTrade = tradeCaptor.getValue();
        assertThat(capturedTrade)
                .usingRecursiveComparison()
                .ignoringFields("timestamp", "id")
                .isEqualTo(new Trade(2L, 1L, new BigDecimal(100), new BigDecimal(5), null));
    }

    @Test
    public void shouldPartiallyMatchOrdersWhenBidPriceIsHigherBestAskPrice() {
        LimitOrder sellOrder = new LimitOrder(1L, 100L, "ticker", Direction.ASK, new BigDecimal(98), new BigDecimal(5), null);
        LimitOrder buyOrder = new LimitOrder(2L, 200L, "ticker", Direction.BID, new BigDecimal(100), new BigDecimal(10), null);

        orderBook.addOrder(sellOrder);
        orderBook.addOrder(buyOrder);

        assertThat(orderBook.getBidsMap()).hasSize(1);
        assertThat(orderBook.getBidsMap().get(new BigDecimal(100))).hasSize(1);
        assertThat(orderBook.getBidsMap().get(new BigDecimal(100)).peek().getPendingQuantity())
                .isEqualTo(new BigDecimal(5));

        verify(tradeListener).onTrade(tradeCaptor.capture());
        Trade capturedTrade = tradeCaptor.getValue();
        assertThat(capturedTrade)
                .usingRecursiveComparison()
                .ignoringFields("timestamp", "id")
                .isEqualTo(new Trade(2L, 1L, new BigDecimal(98), new BigDecimal(5), null));
    }

    @Test
    void shouldCallAllTradeListeners() {
        LimitOrder buyOrder = new LimitOrder(1L, 100L, "ticker", Direction.BID, new BigDecimal(100), new BigDecimal(10), null);
        LimitOrder sellOrder = new LimitOrder(2L, 200L, "ticker", Direction.ASK, new BigDecimal(100), new BigDecimal(10), null);

        var tradeListener1 = mock(TradeListener.class);
        var tradeListener2 = mock(TradeListener.class);
        OrderBook orderBook = new OrderBook("ticker", List.of(tradeListener1, tradeListener2));
        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);

        verify(tradeListener1).onTrade(any(Trade.class));
        verify(tradeListener2).onTrade(any(Trade.class));
    }

    @Test
    void shouldNotAllowEmptyOrders() {
        assertThrows(NullPointerException.class, () -> orderBook.addOrder(null));
    }

    @Test
    void shouldNotAllowOrderWithoutId() {
        assertThrows(IllegalArgumentException.class, () -> orderBook.addOrder(new LimitOrder(null, "", null, null, BigDecimal.TEN, null)));
    }
}