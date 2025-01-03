package xyz.a5s7.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.a5s7.domain.model.Direction;
import xyz.a5s7.domain.model.LimitOrder;
import xyz.a5s7.domain.repository.OrderRepo;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Spy
    private OrderRepo orderRepo;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("should place order successfully")
    void shouldPlaceOrderSuccessfully() {
        // Given
        LimitOrder requestOrder = new LimitOrder(999L, "XYZ", Direction.ASK, new BigDecimal("43251.00"), new BigDecimal("1.0"), ZonedDateTime.now());

        // When
        LimitOrder order = orderService.placeOrder(requestOrder);

        // Then
        assertThat(order).usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(new LimitOrder(1L, 999L, "XYZ", Direction.ASK, new BigDecimal("43251.00"), new BigDecimal("1.0"), ZonedDateTime.now()));
    }

    @Test
    @DisplayName("should find order successfully")
    void shouldFindOrderSuccessfully() {
        // Given
        LimitOrder mockOrder = new LimitOrder(1L, 999L, "ETH", Direction.ASK, new BigDecimal("43251.00"), new BigDecimal("1.0"), ZonedDateTime.now());
        when(orderRepo.findById(1L)).thenReturn(Optional.of(mockOrder));

        // When
        Optional<LimitOrder> foundOrder = orderService.findOrder(1L);

        // Then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get()).usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(mockOrder);
    }

    @Test
    @DisplayName("should return empty if order not found")
    void shouldReturnEmptyIfOrderNotFound() {
        // Given
        when(orderRepo.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<LimitOrder> foundOrder = orderService.findOrder(999L);

        // Then
        assertThat(foundOrder).isNotPresent();
    }
}