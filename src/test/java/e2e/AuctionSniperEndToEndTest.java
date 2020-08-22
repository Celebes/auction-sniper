package e2e;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

public class AuctionSniperEndToEndTest {

    private static FakeAuctionServer auction;
    private static GenericContainer openfire;
    private final ApplicationRunner application = new ApplicationRunner();

    @BeforeClass
    public static void beforeClass() {
        openfire = new GenericContainer("celebez/openfire")
                .withExposedPorts(9090, 9091, 5222, 7777);

        openfire.start();

        auction = new FakeAuctionServer(openfire.getMappedPort(5222), "item-54321");
    }

    @AfterClass
    public static void afterClass() {
        openfire.stop();
    }

    @Test
    public void sniperJoinsAuctionUntilAuctionCloses() throws Exception {
        auction.startSellingItem();
        application.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFromSniper();
        auction.announceClosed();
        application.showsSniperHasLostAuction();
    }

    @After
    public void stopAuction() {
        auction.stop();
    }

    @After
    public void stopApplication() {
        application.stop();
    }
}
