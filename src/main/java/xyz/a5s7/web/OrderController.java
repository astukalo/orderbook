package xyz.a5s7.web;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZonedDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import xyz.a5s7.domain.model.LimitOrder;
import xyz.a5s7.domain.service.OrderService;
import xyz.a5s7.web.request.PlaceOrderRequest;
import xyz.a5s7.web.response.OrderResponse;


@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
                @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                @RequestBody PlaceOrderRequest request) {
        Long userId = getUserIdFromToken(authorizationHeader);
        validate(request);
        
        LimitOrder order = orderService.placeOrder(
            new LimitOrder(userId, request.ticker(), request.direction(), request.price(), request.quantity(), ZonedDateTime.now())
        );
        OrderResponse response = toOrderResponse(order);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(order.getId())
            .toUri();
        
        return ResponseEntity
            .created(location)
            .body(response);
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

    private void validate(PlaceOrderRequest request) {
        if (request.ticker() == null) {
            throw new IllegalArgumentException("Ticker must be provided");
        }
        if (request.direction() == null) {
            throw new IllegalArgumentException("Direction must be provided");
        }
        if (request.price() == null) {
            throw new IllegalArgumentException("Price must be provided");
        }
        if (request.quantity() == null) {
            throw new IllegalArgumentException("Quantity must be provided");
        }
        if (request.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable Long orderId, 
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        LimitOrder order = orderService.findOrder(orderId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order with id " + orderId + " not found")
        );
        //check if user is allowed to see this order
        if (!order.getUserId().equals(getUserIdFromToken(authorizationHeader))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not allowed to see this order");
        }
        return toOrderResponse(order);
    }

    private OrderResponse toOrderResponse(final LimitOrder order) {
        return new OrderResponse(
            order.getId(), 
            order.getTimestamp(),
            order.getTicker(), 
            order.getPrice(), 
            order.getQuantity(), 
            order.getDirection(),
            order.getPendingQuantity()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
