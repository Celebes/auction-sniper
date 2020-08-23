package pl.kgurniak.auctionsniper;

import pl.kgurniak.auctionsniper.enums.PriceSource;

public interface AuctionEventListener {
    void auctionClosed();
    void currentPrice(int price, int increment, PriceSource priceSource);
}
