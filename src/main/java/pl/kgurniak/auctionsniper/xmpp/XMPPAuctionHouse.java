package pl.kgurniak.auctionsniper.xmpp;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import pl.kgurniak.auctionsniper.Auction;
import pl.kgurniak.auctionsniper.AuctionHouse;
import pl.kgurniak.auctionsniper.Item;

public class XMPPAuctionHouse implements AuctionHouse {
    public static final String AUCTION_RESOURCE = "Auction";
    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;
    private final XMPPConnection connection;

    public XMPPAuctionHouse(XMPPConnection connection) {
        this.connection = connection;
    }

    public static XMPPAuctionHouse connect(String hostname, String username, String password, String port) throws XMPPException {
        XMPPConnection connection = new XMPPConnection(new ConnectionConfiguration(hostname, Integer.parseInt(port)));
        connection.connect();
        connection.login(username, password, AUCTION_RESOURCE);
        return new XMPPAuctionHouse(connection);
    }

    @Override
    public Auction auctionFor(Item item) {
        return new XMPPAuction(connection, item);
    }

    public void disconnect() {
        connection.disconnect();
    }
}
