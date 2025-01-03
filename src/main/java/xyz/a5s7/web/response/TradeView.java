package xyz.a5s7.web.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record TradeView(BigDecimal quantity, BigDecimal price, ZonedDateTime executedAt) {
}
