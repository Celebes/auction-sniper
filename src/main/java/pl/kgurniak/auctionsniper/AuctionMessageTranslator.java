package pl.kgurniak.auctionsniper;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import pl.kgurniak.auctionsniper.enums.PriceSource;
import pl.kgurniak.auctionsniper.xmpp.XMPPFailureReporter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AuctionMessageTranslator implements MessageListener {
    private final String sniperId;
    private final AuctionEventListener listener;
    private final XMPPFailureReporter failureReporter;

    public AuctionMessageTranslator(String sniperId, AuctionEventListener listener, XMPPFailureReporter failureReporter) {
        this.sniperId = sniperId;
        this.listener = listener;
        this.failureReporter = failureReporter;
    }

    @Override
    public void processMessage(Chat chat, Message message) {
        String messageBody = message.getBody();
        try {
            translate(messageBody);
        } catch (Exception e) {
            failureReporter.cannotTranslateMessage(sniperId, messageBody, e);
            listener.auctionFailed();
        }
    }

    private void translate(String messageBody) throws MissingValueException {
        AuctionEvent event = AuctionEvent.from(messageBody);
        String type = event.type();

        switch (type) {
            case "CLOSE":
                listener.auctionClosed();
                break;
            case "PRICE":
                final int price = event.currentPrice();
                final int increment = event.increment();
                listener.currentPrice(price, increment, event.isFrom(sniperId));
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static class AuctionEvent {
        private final Map<String, String> fields = new HashMap<>();

        public String type() throws MissingValueException {
            return get("Event");
        }

        public int currentPrice() throws MissingValueException {
            return getInt("CurrentPrice");
        }

        public int increment() throws MissingValueException {
            return getInt("Increment");
        }

        public PriceSource isFrom(String sniperId) throws MissingValueException {
            return sniperId.equals(bidder()) ? PriceSource.FromSniper : PriceSource.FromOtherBidder;
        }

        private String bidder() throws MissingValueException {
            return get("Bidder");
        }

        private int getInt(String fieldName) throws MissingValueException {
            return Integer.parseInt(get(fieldName));
        }

        private String get(String fieldName) throws MissingValueException {
            return Optional.ofNullable(fields.get(fieldName)).orElseThrow(() -> new MissingValueException(fieldName));
        }

        private void addField(String field) {
            String[] pair = field.split(":");
            fields.put(pair[0].trim(), pair[1].trim());
        }

        static AuctionEvent from(String messageBody) {
            AuctionEvent event = new AuctionEvent();
            for (String field : fieldsIn(messageBody)) {
                event.addField(field);
            }
            return event;
        }

        static String[] fieldsIn(String messageBody) {
            return messageBody.split(";");
        }
    }

    public static class MissingValueException extends Exception {
        public MissingValueException(String fieldName) {
            super("Missing value for: " + fieldName);
        }
    }
}
