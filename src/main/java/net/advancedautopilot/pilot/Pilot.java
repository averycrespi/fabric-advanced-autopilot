package net.advancedautopilot.pilot;

import net.advancedautopilot.FlightMonitor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Vec3d;

/**
 * Represents a generic pilot with an optional goal.
 */
public abstract class Pilot {

    FlightMonitor monitor = null;

    public Pilot(FlightMonitor monitor) {
        this.monitor = monitor;
    }

    public abstract Text getName();

    public Text getState() {
        return new TranslatableText("text.advancedautopilot.noPilotState");
    }

    public abstract TickResult onClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal);

    public abstract TickResult onInfrequentClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal);

    public void cleanup(MinecraftClient client, PlayerEntity player) {
        // Intentionally left empty
    }
}
