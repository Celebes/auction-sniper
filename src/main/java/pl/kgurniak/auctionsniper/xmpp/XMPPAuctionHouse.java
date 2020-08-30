package pl.kgurniak.auctionsniper.xmpp;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import pl.kgurniak.auctionsniper.Auction;
import pl.kgurniak.auctionsniper.AuctionHouse;
import pl.kgurniak.auctionsniper.Item;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.apache.commons.io.FilenameUtils.getFullPath;

public class XMPPAuctionHouse implements AuctionHouse {
    public static final String AUCTION_RESOURCE = "Auction";
    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;
    public static final String LOG_FILE_NAME = "auction-sniper.log";
    private static final String LOGGER_NAME = "auction-sniper";
    private final XMPPConnection connection;
    private final XMPPFailureReporter failureReporter;

    public XMPPAuctionHouse(XMPPConnection connection) throws XMPPAuctionException {
        this.connection = connection;
        this.failureReporter = new LoggingXMPPFailureReporter(makeLogger());
    }

    public static XMPPAuctionHouse connect(String hostname, String username, String password, String port) throws XMPPException, XMPPAuctionException {
        XMPPConnection connection = new XMPPConnection(new ConnectionConfiguration(hostname, Integer.parseInt(port)));
        connection.connect();
        connection.login(username, password, AUCTION_RESOURCE);
        return new XMPPAuctionHouse(connection);
    }

    @Override
    public Auction auctionFor(Item item) {
        return new XMPPAuction(connection, item, failureReporter);
    }

    public void disconnect() {
        connection.disconnect();
    }

    private Logger makeLogger() throws XMPPAuctionException {
        Logger logger = Logger.getLogger(XMPPAuctionHouse.LOGGER_NAME);
        logger.setUseParentHandlers(false);
        logger.addHandler(simpleFileHandler());
        return logger;
    }

    private FileHandler simpleFileHandler() throws XMPPAuctionException {
        try {
            FileHandler handler = new FileHandler(LOG_FILE_NAME);
            handler.setFormatter(new SimpleFormatter());
            return handler;
        } catch (Exception e) {
            throw new XMPPAuctionException("Could not create logger FileHandler " + getFullPath(LOG_FILE_NAME), e);
        }
    }
}
