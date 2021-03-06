package net.advancedautopilot.pilot;

import net.advancedautopilot.AdvancedAutopilotMod;
import net.advancedautopilot.Config;
import net.advancedautopilot.ConfigManager;
import net.advancedautopilot.FlightMonitor;
import net.advancedautopilot.PilotHelper;
import net.advancedautopilot.message.YieldedMessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Glides towards an optional goal, adjusting pitch to gain height over time.
 */
public class GlidingPilot extends Pilot {

    private enum PullDirection {
        UP,
        DOWN
    }

    private PullDirection pullDirection = PullDirection.DOWN;

    public GlidingPilot(FlightMonitor monitor, TransitionReason reason) {
        super(monitor, reason);
    }

    @Override
    public Text getName() {
        return Text.translatable("text.advancedautopilot.glidingPilot");
    }

    @Override
    public Text getState() {
        switch (pullDirection) {
            case UP:
                return Text.translatable("text.advancedautopilot.pullingUp");
            case DOWN:
                return Text.translatable("text.advancedautopilot.pullingDown");
            default:
                return Text.translatable("text.advancedautopilot.noPilotState");
        }
    }

    @Override
    public TickResult onClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal) {
        Config config = ConfigManager.getCurrentConfig();

        if (!player.isFallFlying()) {
            AdvancedAutopilotMod.LOGGER.info(new YieldedMessage(this, YieldReason.NOT_FLYING));
            return TickResult.YIELD;
        }

        if (goal != null) {
            player.setYaw(PilotHelper.getGoalYaw(player, goal));
        }

        // Height is updated during onInfrequentClientTick, so it will be out of date
        // However, the timing for speed checks is critical, so this logic must be here
        double height = monitor.getHeight();
        double speed = monitor.getSpeed();
        if (pullDirection == PullDirection.UP) {
            if (speed <= config.minSpeedBeforePullingDown) {
                AdvancedAutopilotMod.LOGGER.info("Started pulling down because speed is too low");
                pullDirection = PullDirection.DOWN;
            } else if (height > config.maxHeightBeforePullingDown) {
                AdvancedAutopilotMod.LOGGER.info("Started pulling down because player is too high");
                pullDirection = PullDirection.DOWN;
            }
        } else if (pullDirection == PullDirection.DOWN) {
            if (speed >= config.maxSpeedBeforePullingUp && height <= config.maxHeightBeforePullingDown) {
                AdvancedAutopilotMod.LOGGER.info("Started pulling up because speed is too high");
                pullDirection = PullDirection.UP;
            } else if (height < config.minHeightBeforePullingUp) {
                AdvancedAutopilotMod.LOGGER.info("Started pulling up because player is too low");
                pullDirection = PullDirection.UP;
            }
        }

        if (pullDirection == PullDirection.UP) {
            player.setPitch((float) MathHelper.wrapDegrees(player.getPitch() - config.pullUpAngularSpeed));
            if (player.getPitch() <= config.pullUpPitch) {
                player.setPitch((float) MathHelper.wrapDegrees(config.pullUpPitch));
            }
        } else if (pullDirection == PullDirection.DOWN) {
            player.setPitch((float) MathHelper.wrapDegrees(player.getPitch() + config.pullDownAngularSpeed));
            if (player.getPitch() >= config.pullDownPitch) {
                player.setPitch((float) MathHelper.wrapDegrees(config.pullDownPitch));
            }
        }

        if (config.poweredFlight) {
            if (PilotHelper.isHoldingFireworkRocket(player)) {
                if (speed <= config.maxPoweredFlightSpeed) {
                    client.options.useKey.setPressed(true);
                } else {
                    client.options.useKey.setPressed(false);
                }
            } else {
                client.options.useKey.setPressed(false);
            }
        }

        return TickResult.CONTINUE;
    }

    public TickResult onInfrequentClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal) {
        Config config = ConfigManager.getCurrentConfig();

        if (goal != null && monitor.getHorizontalDistanceToGoal() < 20) {
            AdvancedAutopilotMod.LOGGER.info(new YieldedMessage(this, YieldReason.NEAR_GOAL));
            return TickResult.YIELD;
        }

        int timeInUnloadedChunks = monitor.getTimeInUnloadedChunks();
        if (config.allowUnloadedChunks && timeInUnloadedChunks > config.maxTimeInUnloadedChunks) {
            AdvancedAutopilotMod.LOGGER.info(new YieldedMessage(this, YieldReason.MAX_TIME_IN_UNLOADED_CHUNKS_ELAPSED));
            return TickResult.YIELD;
        } else if (!config.allowUnloadedChunks && timeInUnloadedChunks > 0) {
            AdvancedAutopilotMod.LOGGER.info(new YieldedMessage(this, YieldReason.IN_UNLOADED_CHUNK));
            return TickResult.YIELD;
        }

        double height = monitor.getHeight();
        if (height < config.minHeightWhileGliding) {
            AdvancedAutopilotMod.LOGGER.info(new YieldedMessage(this, YieldReason.TOO_LOW));
            return TickResult.YIELD;
        }

        return TickResult.CONTINUE;
    }

    @Override
    public void cleanup(MinecraftClient client, PlayerEntity player) {
        super.cleanup(client, player);
        client.options.useKey.setPressed(false);
    }
}
