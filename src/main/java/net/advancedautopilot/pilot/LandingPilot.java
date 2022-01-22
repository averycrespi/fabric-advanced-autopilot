package net.advancedautopilot.pilot;

import net.advancedautopilot.AdvancedAutopilotMod;
import net.advancedautopilot.Config;
import net.advancedautopilot.ConfigManager;
import net.advancedautopilot.FlightMonitor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
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
    public Text getName() {
        return new TranslatableText("text.advancedautopilot.landingPilot");
    }

    @Override
    public TickResult onClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal) {

        if (!player.isFallFlying()) {
            AdvancedAutopilotMod.LOGGER.info("Yielded because player is not flying");
            return TickResult.YIELD;
        }

        return TickResult.CONTINUE;
    }

    @Override
    public TickResult onInfrequentClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal) {
        Config config = ConfigManager.getCurrentConfig();

        double height = monitor.getHeight();
        if (config.riskyLanding && height >= config.minRiskyLandingHeight) {
            player.setPitch(90f); // Look straight down
        } else {
            player.setPitch((float) MathHelper.wrapDegrees(config.landingPitch));
        }

        double speed = monitor.getSpeed();
        if (speed > config.maxLandingSpeed) {
            float oppositeYaw = (float) MathHelper.wrapDegrees(player.getYaw() + 180f);
            player.setYaw(oppositeYaw); // Turn around
        }

        return TickResult.CONTINUE;
    }

    @Override
    public void cleanup(MinecraftClient client, PlayerEntity player) {
        super.cleanup(client, player);
        player.setPitch(0f); // Look forwards
    }
}
