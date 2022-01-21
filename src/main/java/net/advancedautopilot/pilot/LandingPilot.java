package net.advancedautopilot.pilot;

import net.advancedautopilot.AdvancedAutopilotMod;
import net.advancedautopilot.Config;
import net.advancedautopilot.ConfigManager;
import net.advancedautopilot.FlightMonitor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Safely lands the player.
 */
public class LandingPilot extends Pilot {

    public LandingPilot(FlightMonitor monitor) {
        super(monitor);
    }

    @Override
    public TickResult onClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal) {
        Config config = ConfigManager.getCurrentConfig();

        if (!player.isFallFlying()) {
            AdvancedAutopilotMod.LOGGER.info("Yielded because player is not flying");
            return TickResult.YIELD;
        }

        player.setPitch((float) MathHelper.wrapDegrees(config.landingPitch));

        return TickResult.CONTINUE;
    }

    @Override
    public TickResult onInfrequentClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal) {
        Config config = ConfigManager.getCurrentConfig();

        double speed = monitor.getSpeed();
        if (speed > config.maxLandingSpeed) {
            float oppositeYaw = (float) MathHelper.wrapDegrees(player.getYaw() + 180f);
            player.setYaw(oppositeYaw); // Turn around
        }

        return TickResult.CONTINUE;
    }
}
