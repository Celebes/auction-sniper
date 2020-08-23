package e2e;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

public class AuctionSniperEndToEndTest {

    private static final int OPENFIRE_PORT = 5222;
    private final GenericContainer openfire = new GenericContainer("celebez/openfire:4.6.0.beta").withExposedPorts(OPENFIRE_PORT);
    private final ApplicationRunner application = new ApplicationRunner();
    private FakeAuctionServer auction;

    @Before
    public void before() {
        openfire.start();
        auction = new FakeAuctionServer(openfire.getMappedPort(OPENFIRE_PORT), "item-54321");
    }

    @Test
    public void sniperJoinsAuctionUntilAuctionCloses() throws Exception {
        auction.startSellingItem();

        application.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);

        auction.announceClosed();
        application.showsSniperHasLostAuction();
    }

    @Test
    public void sniperMakesAHigherBidButLoses() throws Exception {
        auction.startSellingItem();

        application.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);

        auction.reportPrice(1000, 98, "other bidder");
        application.hasShownSniperIsBidding(1000, 1098);

        auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);

        auction.announceClosed();
        application.showsSniperHasLostAuction();
    }

    @Test
    public void sniperWinsAnAuctionByBiddingHigher() throws Exception {
        auction.startSellingItem();

        application.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);

        auction.reportPrice(1000, 98, "other bidder");
        application.hasShownSniperIsBidding(1000, 1098);

        auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);

        auction.reportPrice(1098, 97, ApplicationRunner.SNIPER_XMPP_ID);
        application.hasShownSniperIsWinning(1098);

        auction.announceClosed();
        application.showsSniperHasWonAuction(1098);
    }

    @After
    public void after() {
        auction.stop();
        application.stop();
        openfire.stop();
    }
}
