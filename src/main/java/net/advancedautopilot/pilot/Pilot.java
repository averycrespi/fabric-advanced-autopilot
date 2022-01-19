package net.advancedautopilot.pilot;

import net.advancedautopilot.FlightMonitor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Represents a generic pilot.
 */
public abstract class Pilot {

    FlightMonitor monitor = null;

    public Pilot(FlightMonitor monitor) {
        this.monitor = monitor;
    }

    public abstract TickResult onClientTick(MinecraftClient client, PlayerEntity player);

    public abstract TickResult onInfrequentClientTick(MinecraftClient client, PlayerEntity player);

    public void reset(MinecraftClient client, PlayerEntity player) {
        // Intentionally left empty
    }
}
