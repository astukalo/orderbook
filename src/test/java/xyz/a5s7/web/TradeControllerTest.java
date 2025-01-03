package xyz.a5s7.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import xyz.a5s7.domain.model.LimitOrder;
import xyz.a5s7.domain.model.Trade;
import xyz.a5s7.domain.service.OrderService;
import xyz.a5s7.domain.service.TradeService;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TradeService tradeService;

    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldReturnTradesForValidOrder() throws Exception {
        LimitOrder order = new LimitOrder(1L, "BTC", null, BigDecimal.ONE, BigDecimal.ONE, ZonedDateTime.now());
        given(orderService.findOrder(1L)).willReturn(Optional.of(order));
        given(tradeService.findTrades(1L)).willReturn(List.of(new Trade(1L, 2L, BigDecimal.ONE, BigDecimal.TEN,
                ZonedDateTime.parse("2025-01-01T00:00:00Z"))));

        mockMvc.perform(get("/trades")
                .param("orderId", "1")
                .header(HttpHeaders.AUTHORIZATION, "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"quantity\":10,\"price\":1,\"executedAt\":\"2025-01-01T00:00:00Z\"}]"));
    }

    @Test
    void shouldReturn404IfOrderNotFound() throws Exception {
        given(orderService.findOrder(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/trades")
                .param("orderId", "999")
                .header(HttpHeaders.AUTHORIZATION, "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnUnauthorizedIfTokenIsIncorrect() throws Exception {
        LimitOrder order = new LimitOrder(1L, "BTC", null, BigDecimal.ONE, BigDecimal.ONE, ZonedDateTime.now());
        given(orderService.findOrder(1L)).willReturn(Optional.of(order));
        mockMvc.perform(get("/trades")
                .param("orderId", "1")
                .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnForbiddenIfUserNotAllowedToViewOrder() throws Exception {
        LimitOrder order = new LimitOrder(1L, "BTC", null, BigDecimal.ZERO, BigDecimal.ZERO, ZonedDateTime.now());
        given(orderService.findOrder(1L)).willReturn(Optional.of(order));

        mockMvc.perform(get("/trades")
                .param("orderId", "1")
                .header(HttpHeaders.AUTHORIZATION, "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnNoContentIfNoTradesFound() throws Exception {
        LimitOrder order = new LimitOrder(1L, "BTC", null, BigDecimal.ZERO, BigDecimal.ZERO, ZonedDateTime.now());
        given(orderService.findOrder(1L)).willReturn(Optional.of(order));
        given(tradeService.findTrades(1L)).willReturn(List.of());

        mockMvc.perform(get("/trades")
                .param("orderId", "1")
                .header(HttpHeaders.AUTHORIZATION, "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
