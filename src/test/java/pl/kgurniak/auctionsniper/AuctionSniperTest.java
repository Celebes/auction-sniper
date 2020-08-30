package pl.kgurniak.auctionsniper;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.States;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pl.kgurniak.auctionsniper.enums.PriceSource;
import pl.kgurniak.auctionsniper.enums.SniperState;

import static org.hamcrest.Matchers.equalTo;

public class AuctionSniperTest {

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    private static final String ITEM_ID = "item-id";
    private static final Item ITEM = new Item(ITEM_ID, 1234);
    private final SniperListener sniperListener = context.mock(SniperListener.class);
    private final Auction auction = context.mock(Auction.class);
    private final AuctionSniper sniper = new AuctionSniper(ITEM, auction);
    private final States sniperState = context.states("sniper");

    @Before
    public void addAuctionSniperListener() {
        sniper.addSniperListener(sniperListener);
    }

    @Test
    public void reportsLostIfAuctionClosesImmediately() {
        context.checking(new Expectations() {{
            atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 0, 0, SniperState.LOST));
        }});

        sniper.auctionClosed();
    }

    @Test
    public void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        final int price = 1001;
        final int increment = 25;
        final int bid = price + increment;

        context.checking(new Expectations() {{
            oneOf(auction).bid(bid);
            atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price, bid, SniperState.BIDDING));
        }});

        sniper.currentPrice(price, increment, PriceSource.FromOtherBidder);
    }

    @Test
    public void reportsIsWinningWhenCurrentPriceComesFromSniper() {
        context.checking(new Expectations() {{
            ignoring(auction);
            allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(SniperState.BIDDING)));
            then(sniperState.is("bidding"));
            atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 135, 135, SniperState.WINNING));
            when(sniperState.is("bidding"));
        }});

        sniper.currentPrice(123, 12, PriceSource.FromOtherBidder);
        sniper.currentPrice(135, 45, PriceSource.FromSniper);
    }

    @Test
    public void reportsLostIfAuctionClosesWhenBidding() {
        context.checking(new Expectations() {{
            ignoring(auction);
            allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(SniperState.BIDDING)));
            then(sniperState.is("bidding"));
            atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 123, 168, SniperState.LOST));
            when(sniperState.is("bidding"));
        }});

        sniper.currentPrice(123, 45, PriceSource.FromOtherBidder);
        sniper.auctionClosed();
    }

    @Test
    public void reportsWonIfAuctionClosesWhenWinning() {
        context.checking(new Expectations() {{
            ignoring(auction);
            allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(SniperState.WINNING)));
            then(sniperState.is("winning"));
            atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 123, 0, SniperState.WON));
            when(sniperState.is("winning"));
        }});

        sniper.currentPrice(123, 45, PriceSource.FromSniper);
        sniper.auctionClosed();
    }

    @Test
    public void doesNotBidAndReportsLosingIfSubsequentPriceIsAboveStopPrice() {
        allowingSniperBidding();
        context.checking(new Expectations() {{
            int bid = 123 + 45;
            allowing(auction).bid(bid);
            atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 2345, bid, SniperState.LOSING));
            when(sniperState.is("bidding"));
        }});

        sniper.currentPrice(123, 45, PriceSource.FromOtherBidder);
        sniper.currentPrice(2345, 25, PriceSource.FromOtherBidder);
    }

    @Test
    public void doesNotBidAndReportsLosingIfFirstPriceIsAboveStopPrice() {
        final int price = 1233;
        final int increment = 25;

        context.checking(new Expectations() {{
            atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price, 0, SniperState.LOSING));
        }});

        sniper.currentPrice(price, increment, PriceSource.FromOtherBidder);
    }

    @Test
    public void continuesToBeLosingOnceStopPriceHasBeenReached() {
        final Sequence states = context.sequence("sniper states");
        final int price1 = 1233;
        final int price2 = 1258;

        context.checking(new Expectations() {{
            atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price1, 0, SniperState.LOSING));
            inSequence(states);
            atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price2, 0, SniperState.LOSING));
            inSequence(states);
        }});

        sniper.currentPrice(price1, 25, PriceSource.FromOtherBidder);
        sniper.currentPrice(price2, 25, PriceSource.FromOtherBidder);
    }

    @Test
    public void doesNotBidAndReportsLosingIfPriceAfterWinningIsAboveStopPrice() {
        final int price = 1233;
        final int increment = 25;

        allowingSniperBidding();
        allowingSniperWinning();
        context.checking(new Expectations() {{
            int bid = 123 + 45;
            allowing(auction).bid(bid);

            atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price, bid, SniperState.LOSING));
            when(sniperState.is("winning"));
        }});

        sniper.currentPrice(123, 45, PriceSource.FromOtherBidder);
        sniper.currentPrice(168, 45, PriceSource.FromSniper);
        sniper.currentPrice(price, increment, PriceSource.FromOtherBidder);
    }

    @Test
    public void reportsFailedIfAuctionFailsWhenBidding() {
        ignoringAuction();
        allowingSniperBidding();

        expectSniperToFailWhenItIs("bidding");

        sniper.currentPrice(123, 45, PriceSource.FromOtherBidder);
        sniper.auctionFailed();
    }

    private Matcher<SniperSnapshot> aSniperThatIs(final SniperState state) {
        return new FeatureMatcher<>(equalTo(state), "sniper that is ", "was") {
            @Override
            protected SniperState featureValueOf(SniperSnapshot actual) {
                return actual.state;
            }
        };
    }

    private void allowingSniperBidding() {
        context.checking(new Expectations() {{
            allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(SniperState.BIDDING)));
            then(sniperState.is("bidding"));
        }});
    }

    private void allowingSniperWinning() {
        context.checking(new Expectations() {{
            allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(SniperState.WINNING)));
            then(sniperState.is("winning"));
        }});
    }

    private void ignoringAuction() {
        context.checking(new Expectations() {{
            ignoring(auction);
        }});
    }

    private void expectSniperToFailWhenItIs(final String state) {
        context.checking(new Expectations() {{
            atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 0, 0, SniperState.FAILED)); when(sniperState.is(state));
        }});
    }
}
