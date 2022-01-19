package net.advancedautopilot.pilot;

import net.advancedautopilot.AdvancedAutopilotMod;
import net.advancedautopilot.ConfigManager;
import net.advancedautopilot.FlightMonitor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

/**
 * Safely lands the player.
 */
public class LandingPilot extends Pilot {

    public LandingPilot(FlightMonitor monitor) {
        super(monitor);
    }

    @Override
    public TickResult onClientTick(MinecraftClient client, PlayerEntity player) {
        if (!player.isFallFlying()) {
            AdvancedAutopilotMod.LOGGER.info("Yielded because player is not flying");
            return TickResult.YIELD;
        }

        player.setPitch(30f); // Slightly downwards

        return TickResult.CONTINUE;
    }

    @Override
    public TickResult onInfrequentClientTick(MinecraftClient client, PlayerEntity player) {
        double speed = monitor.getSpeed();

        if (speed >= ConfigManager.currentConfig.maxLandingSpeed) {
            float oppositeYaw = MathHelper.wrapDegrees(player.getYaw() + 180f);
            player.setYaw(oppositeYaw); // Turn around
        }

        return TickResult.CONTINUE;
    }
}
