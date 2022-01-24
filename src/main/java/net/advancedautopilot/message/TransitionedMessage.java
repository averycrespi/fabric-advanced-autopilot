package net.advancedautopilot.message;

import org.apache.logging.log4j.message.Message;

import net.advancedautopilot.pilot.Pilot;

/**
 * Represents transitioning to a pilot for a reason.
 */
public class TransitionedMessage implements Message {

    private Pilot pilot;
    private String reason;

    public TransitionedMessage(Pilot pilot, String reason) {
        this.pilot = pilot;
        this.reason = reason;
    }

    @Override
    public String getFormattedMessage() {
        if (pilot == null) {
            return String.format("Transitioned because %s", reason);
        } else {
            return String.format("Transitioned to %s because %s", pilot.getName().getString(), reason);
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
