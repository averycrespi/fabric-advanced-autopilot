package net.advancedautopilot.message;

import org.apache.logging.log4j.message.Message;

import net.advancedautopilot.pilot.Pilot;

public class YieldedMessage implements Message {

    private Pilot pilot;
    private String reason;

    public YieldedMessage(Pilot pilot, String reason) {
        this.pilot = pilot;
        this.reason = reason;
    }

    @Override
    public String getFormattedMessage() {
        if (pilot == null) {
            return String.format("Yielded because %s", reason);
        } else {
            return String.format("Yielded from %s because %s", pilot.getName().getString(), reason);
        }
    }

    @Override
    public String getFormat() {
        return (pilot == null ? "" : pilot.getName().getString()) + " " + reason;
    }

    @Override
    public Object[] getParameters() {
        return null;
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }
}
