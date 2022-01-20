package net.advancedautopilot.pilot;

import net.advancedautopilot.AdvancedAutopilotMod;
import net.advancedautopilot.ConfigManager;
import net.advancedautopilot.FlightMonitor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Glides towards an optional goal, adjusting pitch to gain height over time.
 */
public class GlidingPilot extends Pilot {

    private PullDirection pullDirection = PullDirection.DOWN;

    public GlidingPilot(FlightMonitor monitor) {
        super(monitor);
    }

    @Override
    public TickResult onClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal) {
        if (!player.isFallFlying()) {
            AdvancedAutopilotMod.LOGGER.info("Yielded because player is not flying");
            return TickResult.YIELD;
        }

        if (goal != null) {
            player.setYaw(PilotHelper.getGoalYaw(player, goal));
        }

        double speed = monitor.getSpeed();
        if (pullDirection == PullDirection.UP
                && speed <= ConfigManager.currentConfig.minSpeedBeforePullingDown) {
            AdvancedAutopilotMod.LOGGER.info("Started pulling down because speed is too low");
            pullDirection = PullDirection.DOWN;
        } else if (pullDirection == PullDirection.DOWN
                && speed >= ConfigManager.currentConfig.maxSpeedBeforePullingUp) {
            AdvancedAutopilotMod.LOGGER.info("Started pulling up because speed is too high");
            pullDirection = PullDirection.UP;
        }

        if (pullDirection == PullDirection.UP) {
            player.setPitch((float) MathHelper.wrapDegrees(
                    player.getPitch() - ConfigManager.currentConfig.pullUpAngularSpeed));
            if (player.getPitch() <= ConfigManager.currentConfig.pullUpPitch) {
                player.setPitch((float) MathHelper.wrapDegrees(ConfigManager.currentConfig.pullUpPitch));
            }
        } else if (pullDirection == PullDirection.DOWN) {
            player.setPitch((float) MathHelper.wrapDegrees(
                    player.getPitch() + ConfigManager.currentConfig.pullDownAngularSpeed));
            if (player.getPitch() >= ConfigManager.currentConfig.pullDownPitch) {
                player.setPitch((float) MathHelper.wrapDegrees(ConfigManager.currentConfig.pullDownPitch));
            }
        }

        return TickResult.CONTINUE;
    }

    public TickResult onInfrequentClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal) {
        double height = monitor.getHeight();

        if (goal != null && monitor.getHorizontalDistanceToGoal() < 20) {
            AdvancedAutopilotMod.LOGGER.info("Yielded because player is near goal");
            return TickResult.YIELD;
        }

        if (height < ConfigManager.currentConfig.minHeightWhileGliding) {
            AdvancedAutopilotMod.LOGGER.info("Yielded because player is too low");
            return TickResult.YIELD;
        }

        if (pullDirection == PullDirection.UP
                && height > ConfigManager.currentConfig.maxHeightBeforePullingDown) {
            AdvancedAutopilotMod.LOGGER.info("Started pulling down because player is too high");
            pullDirection = PullDirection.DOWN;
        } else if (pullDirection == PullDirection.DOWN
                && height < ConfigManager.currentConfig.minHeightBeforePullingUp) {
            AdvancedAutopilotMod.LOGGER.info("Started pulling up because player is too low");
            pullDirection = PullDirection.UP;
        }

        return TickResult.CONTINUE;
    }
}
