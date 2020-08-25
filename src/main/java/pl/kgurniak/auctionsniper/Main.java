package pl.kgurniak.auctionsniper;

import pl.kgurniak.auctionsniper.ui.MainWindow;
import pl.kgurniak.auctionsniper.ui.SnipersTableModel;
import pl.kgurniak.auctionsniper.xmpp.XMPPAuctionHouse;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    private static final int ARG_HOSTNAME = 0;
    private static final int ARG_USERNAME = 1;
    private static final int ARG_PASSWORD = 2;
    private static final int ARG_PORT = 3;

    private final SniperPortfolio portfolio = new SniperPortfolio();
    private MainWindow ui;

    public Main() throws Exception {
        startUserInterface();
    }

    private void startUserInterface() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                ui = new MainWindow(portfolio);
            }
        });
    }

    private void addRequestListenerFor(final XMPPAuctionHouse auctionHouse) throws Exception {
        ui.addUserRequestListener(new SniperLauncher(auctionHouse, portfolio));
    }

    private void disconnectWhenUICloses(XMPPAuctionHouse auctionHouse) {
        ui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                auctionHouse.disconnect();
            }
        });
    }

    public static void main(String... args) throws Exception {
        Main main = new Main();
        XMPPAuctionHouse auctionHouse = XMPPAuctionHouse.connect(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD], args[ARG_PORT]);
        main.disconnectWhenUICloses(auctionHouse);
        main.addRequestListenerFor(auctionHouse);
    }

}
