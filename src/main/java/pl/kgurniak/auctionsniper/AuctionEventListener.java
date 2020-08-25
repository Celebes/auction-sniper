package pl.kgurniak.auctionsniper;

import pl.kgurniak.auctionsniper.enums.PriceSource;

import java.util.EventListener;

public interface AuctionEventListener extends EventListener {
    void auctionClosed();
    void currentPrice(int price, int increment, PriceSource priceSource);
}
