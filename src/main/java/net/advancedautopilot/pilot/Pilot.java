package net.advancedautopilot.pilot;

import net.advancedautopilot.AdvancedAutopilotMod;
import net.advancedautopilot.FlightMonitor;
import net.advancedautopilot.message.TransitionedMessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

/**
 * Represents a generic pilot with an optional goal.
 */
public abstract class Pilot {

    public enum TickResult {
        CONTINUE,
        YIELD
    }

    /**
     * Reasons why a pilot was transitioned to.
     */
    public enum TransitionReason {
        ABOVE_ASCENT_HEIGHT,
        ENGAGED_AUTOPILOT,
        HOLDING_ROCKETS,
        IN_UNLOADED_CHUNK,
        LAND_COMMAND_SENT,
        NEAR_GOAL,
        NOT_HOLDING_ROCKETS,
        MAX_TIME_IN_UNLOADED_CHUNKS_ELAPSED,
        PERFORMING_EMERGENCY_LANDING,
    }

    /**
     * Reasons why a pilot yielded.
     */
    public enum YieldReason {
        ATTEMPTING_TO_RESUME_FLIGHT_TOWARDS_GOAL,
        IN_UNLOADED_CHUNK,
        MAX_TIME_IN_UNLOADED_CHUNKS_ELAPSED,
        NEAR_GOAL,
        NOT_FLYING,
        OUT_OF_ROCKETS,
        REACHED_ASCENT_HEIGHT,
        TOO_LOW
    }

    FlightMonitor monitor;
    TransitionReason reason;

    public Pilot(FlightMonitor monitor, TransitionReason reason) {
        this.monitor = monitor;
        this.reason = reason;
        AdvancedAutopilotMod.LOGGER.info(new TransitionedMessage(this, reason));
    }

    public abstract Text getName();

    public Text getState() {
        return Text.translatable("text.advancedautopilot.noPilotState");
    }

    public abstract TickResult onClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal);

    public abstract TickResult onInfrequentClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal);

    public void cleanup(MinecraftClient client, PlayerEntity player) {
        AdvancedAutopilotMod.LOGGER.info(String.format("Cleaned up %s", this.getName().getString()));
    }
}
