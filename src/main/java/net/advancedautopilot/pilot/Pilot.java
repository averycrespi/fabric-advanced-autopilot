package net.advancedautopilot.pilot;

import net.advancedautopilot.AdvancedAutopilotMod;
import net.advancedautopilot.FlightMonitor;
import net.advancedautopilot.message.TransitionedMessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Vec3d;

/**
 * Represents a generic pilot with an optional goal.
 */
public abstract class Pilot {

    public enum TickResult {
        CONTINUE,
        YIELD
    }

    FlightMonitor monitor;

    public Pilot(FlightMonitor monitor, String reason) {
        this.monitor = monitor;
        AdvancedAutopilotMod.LOGGER.info(new TransitionedMessage(this, reason));
    }

    public abstract Text getName();

    public Text getState() {
        return new TranslatableText("text.advancedautopilot.noPilotState");
    }

    public abstract TickResult onClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal);

    public abstract TickResult onInfrequentClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal);

    public void cleanup(MinecraftClient client, PlayerEntity player) {
        AdvancedAutopilotMod.LOGGER.info(String.format("Cleaned up %s", this.getName().getString()));
    }
}
