package net.advancedautopilot.pilots;

import net.advancedautopilot.AdvancedAutopilotConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Ascends directly upwards (using fireworks) until the configured ascent height
 * is reached or the player runs out of fireworks.
 */
public class AscendingPilot extends Pilot {

    private static final double MAX_FIREWORK_VERTICAL_SPEED = 15d;

    @Override
    public TickResult onScreenTick(MinecraftClient client, PlayerEntity player, AdvancedAutopilotConfig config) {
        return TickResult.CONTINUE;
    }

    @Override
    public TickResult onClientTick(MinecraftClient client, PlayerEntity player, AdvancedAutopilotConfig config) {
        if (!player.isFallFlying()) {
            client.options.keyJump.setPressed(!client.options.keyJump.isPressed());
            return TickResult.CONTINUE; // Wait for next tick before continuing
        }

        player.setPitch(-90f); // Look directly upwards

        updateVelocity(player, CLIENT_TICKS_PER_SECOND);
        if (PilotHelper.isHoldingFirework(player)) {
            Vec3d velocity = getVelocity();
            if (player.getPitch() == -90f
                    && velocity.getY() < 0
                    && velocity.getY() >= -MAX_FIREWORK_VERTICAL_SPEED) {
                client.options.keyUse.setPressed(true);
            } else {
                client.options.keyUse.setPressed(false);
            }
        } else {
            LOGGER.info("Yielded because player ran out of fireworks");
            return TickResult.YIELD;
        }

        return TickResult.CONTINUE;
    }

    @Override
    public TickResult onInfrequentClientTick(MinecraftClient client, PlayerEntity player,
            AdvancedAutopilotConfig config) {
        updateDistanceToGround(player);
        if (getDistanceToGround() >= config.ascentHeight) {
            LOGGER.info("Yielded because player reached ascent height");
            return TickResult.YIELD;
        }

        return TickResult.CONTINUE;
    }

    @Override
    public void reset(MinecraftClient client, PlayerEntity player) {
        super.reset(client, player);
        client.options.keyUse.setPressed(false);
        client.options.keyJump.setPressed(false);
        player.setPitch(0); // Look forwards
    }
}
