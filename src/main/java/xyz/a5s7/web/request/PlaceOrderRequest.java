package xyz.a5s7.web.request;

import java.math.BigDecimal;

import xyz.a5s7.domain.model.Direction;

/**
 * ticker - string, asset name, for simplicity this can be any text
 * price - number, a price for limit order
 * quantity - number, quantity of asset to fill by order
 * direction - string, can be either "BID" (buy) or "ASK" (sell)
 */
public record PlaceOrderRequest(String ticker, BigDecimal price, BigDecimal quantity, Direction direction) { }
