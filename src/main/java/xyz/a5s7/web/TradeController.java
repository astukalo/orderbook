package xyz.a5s7.web;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import xyz.a5s7.domain.model.LimitOrder;
import xyz.a5s7.domain.service.OrderService;
import xyz.a5s7.domain.service.TradeService;
import xyz.a5s7.web.response.TradeView;

@RestController
@RequestMapping("/trades")
public class TradeController {
    private final TradeService tradeService;
    private final OrderService orderService;

    public TradeController(TradeService tradeService, OrderService orderService) {
        this.tradeService = tradeService;
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<TradeView>> getTradesByOrderId(
            @RequestParam Long orderId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader    
        ) {
        LimitOrder order = orderService.findOrder(orderId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order with id " + orderId + " not found")
        );
        //check if user is allowed to see this order
        if (!order.getUserId().equals(getUserIdFromToken(authorizationHeader))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not allowed to see this order");
        }

        var trades = tradeService
                .findTrades(orderId)
                .stream()
                .map(trade -> new TradeView(
                    trade.quantity(),
                    trade.price(),
                    trade.timestamp()
                ))
                .toList();
        if (trades.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(trades);
    }

    /**
     * Extracts user id from token.
     * Just for simplicity we will use token as user id
     * @param token
     * @return user id
     */
    private Long getUserIdFromToken(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return Long.parseLong(token);
    }
} 