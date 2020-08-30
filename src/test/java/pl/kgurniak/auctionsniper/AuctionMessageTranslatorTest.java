package pl.kgurniak.auctionsniper;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import pl.kgurniak.auctionsniper.enums.PriceSource;
import pl.kgurniak.auctionsniper.xmpp.XMPPAuction;
import pl.kgurniak.auctionsniper.xmpp.XMPPFailureReporter;

import static e2e.ApplicationRunner.SNIPER_ID;

public class AuctionMessageTranslatorTest {

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    public static final Chat UNUSED_CHAT = null;

    private final AuctionEventListener listener = context.mock(AuctionEventListener.class);
    private final XMPPFailureReporter failureReporter = context.mock(XMPPFailureReporter.class);
    private final AuctionMessageTranslator translator = new AuctionMessageTranslator(SNIPER_ID, listener, failureReporter);

    @Test
    public void notifiesAuctionClosedWhenCloseMessageReceived() {
        context.checking(new Expectations() {{
            oneOf(listener).auctionClosed();
        }});

        Message message = new Message();
        message.setBody("SOLVersion: 1.1; Event: CLOSE;");

        translator.processMessage(UNUSED_CHAT, message);
    }

    @Test
    public void notifiesBidDetailsWhenCurrentPriceMessageReceived() {
        context.checking(new Expectations() {{
            exactly(1).of(listener).currentPrice(192, 7, PriceSource.FromOtherBidder);
        }});

        Message message = new Message();
        message.setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;");
        translator.processMessage(UNUSED_CHAT, message);
    }

    @Test
    public void notifiesBidDetailsWhenCurrentPriceMessageReceivedFromSniper() {
        final int price = 234;
        final int increment = 5;
        context.checking(new Expectations() {{
            exactly(1).of(listener).currentPrice(price, increment, PriceSource.FromSniper);
        }});
        Message message = new Message();
        message.setBody(String.format(XMPPAuction.PRICE_COMMAND_FORMAT, price, increment, SNIPER_ID));
        translator.processMessage(UNUSED_CHAT, message);
    }

    @Test
    public void notifiesAuctionFailedWhenBadMessageReceived() {
        String badMessage = "a bad message";
        expectFailureWithMessage(badMessage);
        translator.processMessage(UNUSED_CHAT, message(badMessage));
    }

    @Test
    public void notifiesAuctionFailedWhenEventTypeMissing() {
        final String badMessage = "SOLVersion: 1.1; CurrentPrice: 234; Increment: 5; Bidder: " + SNIPER_ID + ";";
        expectFailureWithMessage(badMessage);
        translator.processMessage(UNUSED_CHAT, message(badMessage));
    }

    private void expectFailureWithMessage(final String badMessage) {
        context.checking(new Expectations() {{
            oneOf(listener).auctionFailed();
            oneOf(failureReporter).cannotTranslateMessage(with(SNIPER_ID), with(badMessage), with(any(Exception.class)));
        }});
    }

    private Message message(String body) {
        Message message = new Message();
        message.setBody(body);
        return message;
    }
}
