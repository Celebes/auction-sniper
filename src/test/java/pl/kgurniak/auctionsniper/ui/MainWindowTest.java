package pl.kgurniak.auctionsniper.ui;

import com.objogate.wl.swing.probe.ValueMatcherProbe;
import e2e.AuctionSniperDriver;
import org.junit.Test;
import pl.kgurniak.auctionsniper.Item;
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
        final ValueMatcherProbe<Item> itemProbe = new ValueMatcherProbe<>(equalTo(
                new Item("some item-id", 789)),
                "join request"
        );
        mainWindow.addUserRequestListener(new UserRequestListener() {
            public void joinAuction(Item item) {
                itemProbe.setReceivedValue(item);
            }
        });
        driver.startBiddingFor("some item-id", 789);
        driver.check(itemProbe);
    }
}
