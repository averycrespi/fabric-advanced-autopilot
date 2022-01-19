package net.advancedautopilot.pilot;

import net.advancedautopilot.AdvancedAutopilotMod;
import net.advancedautopilot.ConfigManager;
import net.advancedautopilot.FlightMonitor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Glides towards an optional goal, adjusting pitch to gain height over time.
 */
public class GlidingPilot extends PilotWithGoal {

    private PullDirection pullDirection = PullDirection.DOWN;

    public GlidingPilot(FlightMonitor monitor) {
        super(monitor);
    }

    @Override
    public TickResult onClientTick(MinecraftClient client, PlayerEntity player) {
        if (!player.isFallFlying()) {
            AdvancedAutopilotMod.LOGGER.info("Yielded because player is not flying");
            return TickResult.YIELD;
        }

        if (hasGoal()) {
            if (getDistanceToGoal(player) < 20) {
                AdvancedAutopilotMod.LOGGER.info("Yielded because player is near goal");
                return TickResult.YIELD;
            }
            lookTowardsGoal(player);
        }

        switch (pullDirection) {
            case UP:
                player.setPitch((float) ConfigManager.currentConfig.pullUpPitch);
                break;
            case DOWN:
                player.setPitch((float) ConfigManager.currentConfig.pullDownPitch);
                break;
        }

        return TickResult.CONTINUE;
    }

    public TickResult onInfrequentClientTick(MinecraftClient client, PlayerEntity player) {
        double speed = monitor.getSpeed();
        double height = monitor.getHeight();
        if (pullDirection == PullDirection.UP) {
            if (speed < ConfigManager.currentConfig.minSpeedBeforePullingDown) {
                AdvancedAutopilotMod.LOGGER.info("Started pulling down because speed is too low");
                pullDirection = PullDirection.DOWN;
            } else if (height >= ConfigManager.currentConfig.maxHeightWhileGliding) {
                AdvancedAutopilotMod.LOGGER.info("Started pulling down because player is too high");
                pullDirection = PullDirection.DOWN;
            }
        } else if (pullDirection == PullDirection.DOWN) {
            if (speed > ConfigManager.currentConfig.maxSpeedBeforePullingUp) {
                AdvancedAutopilotMod.LOGGER.info("Started pulling up because speed is too high");
                pullDirection = PullDirection.UP;
            }
        }

        return TickResult.CONTINUE;
    }
}
