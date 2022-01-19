package net.advancedautopilot.pilot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.advancedautopilot.AdvancedAutopilotConfig;
import net.advancedautopilot.FlightMonitor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Represents a generic pilot.
 */
public abstract class Pilot {
    public static final Logger LOGGER = LogManager.getLogger("advancedautopilot.Pilot");

    AdvancedAutopilotConfig config = null;
    FlightMonitor monitor = null;

    public Pilot(AdvancedAutopilotConfig config, FlightMonitor monitor) {
        this.config = config;
        this.monitor = monitor;
    }

    public abstract TickResult onClientTick(MinecraftClient client, PlayerEntity player);

    public abstract TickResult onInfrequentClientTick(MinecraftClient client, PlayerEntity player);

    public void setConfig(AdvancedAutopilotConfig config) {
        this.config = config;
    }

    public void reset(MinecraftClient client, PlayerEntity player) {
        // Intentionally left empty
    }
}
