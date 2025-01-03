package xyz.a5s7.web.response;

import xyz.a5s7.domain.model.Direction;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record OrderResponse(Long id, ZonedDateTime timestamp, String ticker, BigDecimal price, BigDecimal quantity,
                            Direction direction, BigDecimal pendingQuantity) {
}
