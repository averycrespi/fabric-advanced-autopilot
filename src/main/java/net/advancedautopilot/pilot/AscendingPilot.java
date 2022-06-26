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
import net.minecraft.util.math.Vec3d;

/**
 * Ascends directly upwards (using fireworks) until the configured ascent height
 * is reached or the player runs out of fireworks.
 */
public class AscendingPilot extends Pilot {

    public AscendingPilot(FlightMonitor monitor, TransitionReason reason) {
        super(monitor, reason);
    }

    @Override
    public Text getName() {
        return Text.translatable("text.advancedautopilot.ascendingPilot");
    }

    @Override
    public TickResult onClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal) {
        Config config = ConfigManager.getCurrentConfig();

        if (!player.isFallFlying()) {
            client.options.jumpKey.setPressed(!client.options.jumpKey.isPressed());
            return TickResult.CONTINUE; // Wait for next tick before continuing
        }

        if (monitor.getHeight() >= config.ascentHeight) {
            AdvancedAutopilotMod.LOGGER.info(new YieldedMessage(this, YieldReason.REACHED_ASCENT_HEIGHT));
            return TickResult.YIELD;
        }

        Vec3d velocity = monitor.getVelocity();
        if (PilotHelper.isHoldingFireworkRocket(player)) {
            if (velocity.getY() <= config.maxAscendingVerticalVelocity) {
                player.setPitch(-90f); // Look upwards
                client.options.useKey.setPressed(true);
            } else {
                client.options.useKey.setPressed(false);
            }
        } else if (config.refillRockets && PilotHelper.swapFireworkRocketsIntoMainHand(client, player)) {
            // Wait for next tick before using rocket
            client.options.useKey.setPressed(false);
        } else {
            AdvancedAutopilotMod.LOGGER.info(new YieldedMessage(this, YieldReason.OUT_OF_ROCKETS));
            return TickResult.YIELD;
        }

        return TickResult.CONTINUE;
    }

    public TickResult onInfrequentClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal) {
        Config config = ConfigManager.getCurrentConfig();

        int timeInUnloadedChunks = monitor.getTimeInUnloadedChunks();
        if (config.allowUnloadedChunks && timeInUnloadedChunks > config.maxTimeInUnloadedChunks) {
            AdvancedAutopilotMod.LOGGER.info(new YieldedMessage(this, YieldReason.MAX_TIME_IN_UNLOADED_CHUNKS_ELAPSED));
            return TickResult.YIELD;
        } else if (!config.allowUnloadedChunks && timeInUnloadedChunks > 0) {
            AdvancedAutopilotMod.LOGGER.info(new YieldedMessage(this, YieldReason.IN_UNLOADED_CHUNK));
            return TickResult.YIELD;
        }

        return TickResult.CONTINUE;
    }

    @Override
    public void cleanup(MinecraftClient client, PlayerEntity player) {
        super.cleanup(client, player);
        client.options.useKey.setPressed(false);
        client.options.jumpKey.setPressed(false);
        player.setPitch(0); // Look forwards
    }
}
