package pl.kgurniak.auctionsniper;

import pl.kgurniak.auctionsniper.util.Announcer;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class SniperPortfolio implements SniperCollector {
    private final List<AuctionSniper> snipers = new ArrayList<>();
    private final Announcer<PortfolioListener> announcer = Announcer.to(PortfolioListener.class);

    @Override
    public void addSniper(AuctionSniper sniper) {
        snipers.add(sniper);
        announcer.announce().sniperAdded(sniper);
    }

    public void addPortfolioListener(PortfolioListener listener) {
        announcer.addListener(listener);
    }
}
