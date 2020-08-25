package pl.kgurniak.auctionsniper;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import pl.kgurniak.auctionsniper.ui.MainWindow;
import pl.kgurniak.auctionsniper.ui.SnipersTableModel;
import pl.kgurniak.auctionsniper.xmpp.XMPPAuction;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final int ARG_HOSTNAME = 0;
    private static final int ARG_USERNAME = 1;
    private static final int ARG_PASSWORD = 2;
    private static final int ARG_PORT = 3;

    public static final String AUCTION_RESOURCE = "Auction";
    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;
    public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
    public static final String CLOSE_COMMAND_FORMAT = "SOLVersion: 1.1; Event: CLOSE;";
    public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;";
    public static final String PRICE_COMMAND_FORMAT = "SOLVersion: 1.1; Event: PRICE; CurrentPrice: %d; Increment: %d; Bidder: %s;";

    private final SnipersTableModel snipers = new SnipersTableModel();
    private MainWindow ui;
    private List<Auction> notToBeGCd = new ArrayList<>();

    public Main() throws Exception {
        startUserInterface();
    }

    private void startUserInterface() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                ui = new MainWindow(snipers);
            }
        });
    }

    private void addRequestListenerFor(final XMPPConnection connection) throws Exception {
        ui.addUserRequestListener(new UserRequestListener() {
            @Override
            public void joinAuction(String itemId) {
                snipers.addSniper(SniperSnapshot.joining(itemId));
                Auction auction = new XMPPAuction(connection, itemId);
                notToBeGCd.add(auction);
                auction.addAuctionEventListener(new AuctionSniper(auction, new SwingThreadSniperListener(snipers), itemId));
                auction.join();
            }
        });
    }

    private void disconnectWhenUICloses(XMPPConnection connection) {
        ui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                connection.disconnect();
            }
        });
    }

    public static void main(String... args) throws Exception {
        Main main = new Main();
        final XMPPConnection connection = connection(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD], args[ARG_PORT]);
        main.disconnectWhenUICloses(connection);
        main.addRequestListenerFor(connection);
    }

    private static XMPPConnection connection(String hostname, String username, String password, String port) throws XMPPException {
        XMPPConnection connection = new XMPPConnection(new ConnectionConfiguration(hostname, Integer.parseInt(port)));
        connection.connect();
        connection.login(username, password, AUCTION_RESOURCE);
        return connection;
    }

    public class SwingThreadSniperListener implements SniperListener {
        private final SniperListener sniperListener;

        public SwingThreadSniperListener(SniperListener sniperListener) {
            this.sniperListener = sniperListener;
        }

        @Override
        public void sniperStateChanged(final SniperSnapshot snapshot) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    sniperListener.sniperStateChanged(snapshot);
                }
            });
        }
    }
}
