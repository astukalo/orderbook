package xyz.a5s7.web;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import xyz.a5s7.domain.model.LimitOrder;
import xyz.a5s7.domain.model.Direction;
import xyz.a5s7.domain.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    public void shouldPlaceOrderAndReturnCreatedStatus() throws Exception {
        String orderRequest = """
            {
              "ticker": "BTC",
              "price": 43251.00,
              "quantity": 1.65,
              "direction": "ASK"
            }
            """;

        String expectedResponse = """
            {
              "id": 1,
              "timestamp": "2024-12-08T13:34:44.498770729Z",
              "ticker": "BTC",
              "price": 43251.00,
              "quantity": 1.65,
              "direction": "ASK",
              "pendingQuantity": 1.65
            }
            """;

        given(orderService.placeOrder(any()))
            .willReturn(
                new LimitOrder(1L, 999L, "BTC", Direction.ASK, new BigDecimal("43251.00"), new BigDecimal("1.65"), 
                    ZonedDateTime.parse("2024-12-08T13:34:44.498770729Z")
                )
            );

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderRequest)
                .header("Authorization", "999"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/orders/1"))
                .andExpect(content().json(expectedResponse));
    }

    @Test
    void shouldReturn404IfOrderNotFound() throws Exception {
        given(orderService.findOrder(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/orders/999")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestIfIncorrectValues() throws Exception {
        String orderRequest = """
            {
              "ticker": "BTC",
              "price": 43251.00,
              "quantity": -0.65,
              "direction": "BID"
            }
            """;

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderRequest)
                .header("Authorization", "999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Quantity must be greater than 0"));
    }

    @Test
    void shouldReturnUnauthorizedIfTokenIsEmpty() throws Exception {
        String orderRequest = """
            {
              "ticker": "BTC",
              "price": 43251.00,
              "quantity": 1.65,
              "direction": "ASK"
            }
            """;

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderRequest)
                .header("Authorization", ""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnForbiddenIfUserNotAllowedToViewOrder() throws Exception {
        LimitOrder order = new LimitOrder(1L, "BTC", Direction.ASK, new BigDecimal("43251.00"),
                new BigDecimal("1.65"), ZonedDateTime.now());
        given(orderService.findOrder(1L)).willReturn(Optional.of(order));

        mockMvc.perform(get("/orders/1")
                .header("Authorization", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
