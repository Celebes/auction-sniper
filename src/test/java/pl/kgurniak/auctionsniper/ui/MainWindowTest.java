package pl.kgurniak.auctionsniper.ui;

import com.objogate.wl.swing.probe.ValueMatcherProbe;
import e2e.AuctionSniperDriver;
import org.junit.Test;
import pl.kgurniak.auctionsniper.SniperPortfolio;
import pl.kgurniak.auctionsniper.UserRequestListener;

import static org.hamcrest.Matchers.equalTo;

public class MainWindowTest {
    private final SniperPortfolio portfolio = new SniperPortfolio();
    private final MainWindow mainWindow = new MainWindow(portfolio);
    private final AuctionSniperDriver driver = new AuctionSniperDriver(100);

    static {
        System.setProperty("com.objogate.wl.keyboard", "GB");
    }

    @Test
    public void makesUserRequestWhenJoinButtonClicked() {
        final ValueMatcherProbe<String> buttonProbe = new ValueMatcherProbe<>(equalTo("some item-id"), "join request");
        mainWindow.addUserRequestListener(new UserRequestListener() {
            public void joinAuction(String itemId) {
                buttonProbe.setReceivedValue(itemId);
            }
        });
        driver.startBiddingFor("some item-id");
        driver.check(buttonProbe);
    }
}
