package net.advancedautopilot.message;

import org.apache.logging.log4j.message.Message;

import net.advancedautopilot.pilot.Pilot;

/**
 * Represents transitioning to a pilot for a reason.
 */
public class TransitionedMessage implements Message {

    private Pilot pilot;
    private Pilot.TransitionReason reason;

    public TransitionedMessage(Pilot pilot, Pilot.TransitionReason reason) {
        this.pilot = pilot;
        this.reason = reason;
    }

    @Override
    public String getFormattedMessage() {
        if (pilot == null) {
            return String.format("Transitioned because %s", reason.toString());
        } else {
            return String.format("Transitioned to %s because %s", pilot.getName().getString(), reason.toString());
        }
    }

    @Override
    public String getFormat() {
        return (pilot == null ? "" : pilot.getName().getString()) + " " + reason.toString();
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
