package pl.kgurniak.auctionsniper.xmpp;

import e2e.ApplicationRunner;
import e2e.FakeAuctionServer;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import pl.kgurniak.auctionsniper.Auction;
import pl.kgurniak.auctionsniper.AuctionEventListener;
import pl.kgurniak.auctionsniper.Main;
import pl.kgurniak.auctionsniper.enums.PriceSource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class XMPPAuctionTest {
    private static final int OPENFIRE_PORT = 5222;
    private final GenericContainer openfire = new GenericContainer("celebez/openfire:4.6.0.beta").withExposedPorts(OPENFIRE_PORT);
    private FakeAuctionServer auction;
    private XMPPConnection connection;

    static {
        System.setProperty("com.objogate.wl.keyboard", "GB");
    }

    @Before
    public void before() throws XMPPException {
        openfire.start();
        final Integer openfirePort = openfire.getMappedPort(OPENFIRE_PORT);
        auction = new FakeAuctionServer(openfirePort, "item-54321");
        auction.startSellingItem();

        connection = new XMPPConnection(new ConnectionConfiguration(FakeAuctionServer.XMPP_HOSTNAME, openfirePort));
        connection.connect();
        connection.login(ApplicationRunner.SNIPER_ID, ApplicationRunner.SNIPER_PASSWORD, Main.AUCTION_RESOURCE);
    }

    @Test
    public void receivesEventsFromAuctionServerAfterJoining() throws Exception {
        CountDownLatch auctionWasClosed = new CountDownLatch(1);
        Auction auction = new XMPPAuction(connection, this.auction.getItemId());
        auction.addAuctionEventListener(auctionClosedListener(auctionWasClosed));
        auction.join();
        this.auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);
        this.auction.announceClosed();

        assertTrue("should have been closed", auctionWasClosed.await(2, TimeUnit.SECONDS));
    }

    private AuctionEventListener auctionClosedListener(final CountDownLatch auctionWasClosed) {
        return new AuctionEventListener() {
            public void auctionClosed() {
                auctionWasClosed.countDown();
            }

            public void currentPrice(int price, int increment, PriceSource priceSource) {
                // not implemented
            }
        };
    }

    @After
    public void closeConnection() {
        if (connection != null) {
            connection.disconnect();
        }
        auction.stop();
        openfire.stop();
    }
}
