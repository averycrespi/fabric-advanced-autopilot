package net.advancedautopilot.pilot;

import net.advancedautopilot.AdvancedAutopilotMod;
import net.advancedautopilot.Config;
import net.advancedautopilot.ConfigManager;
import net.advancedautopilot.FlightMonitor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Ascends directly upwards (using fireworks) until the configured ascent height
 * is reached or the player runs out of fireworks.
 *
 * TODO: avoid splatting when the player opens chat while using fireworks
 */
public class AscendingPilot extends Pilot {

    private static final double MAX_FIREWORK_VERTICAL_VELOCITY = 15d;

    public AscendingPilot(FlightMonitor monitor) {
        super(monitor);
    }

    @Override
    public TickResult onClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal) {
        Config config = ConfigManager.getCurrentConfig();

        if (!player.isFallFlying()) {
            client.options.keyJump.setPressed(!client.options.keyJump.isPressed());
            return TickResult.CONTINUE; // Wait for next tick before continuing
        }

        if (monitor.getHeight() >= config.ascentHeight) {
            AdvancedAutopilotMod.LOGGER.info("Yielded because player reached ascent height");
            return TickResult.YIELD;
        }

        Vec3d velocity = monitor.getVelocity();
        if (PilotHelper.isHoldingFirework(player)) {
            if (velocity.getY() <= MAX_FIREWORK_VERTICAL_VELOCITY) {
                player.setPitch(-90f); // Look upwards
                client.options.keyUse.setPressed(true);
            } else {
                client.options.keyUse.setPressed(false);
            }
        } else {
            AdvancedAutopilotMod.LOGGER.info("Yielded because player ran out of fireworks");
            return TickResult.YIELD;
        }

        return TickResult.CONTINUE;
    }

    public TickResult onInfrequentClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal) {
        return TickResult.CONTINUE;
    }

    @Override
    public void cleanup(MinecraftClient client, PlayerEntity player) {
        super.cleanup(client, player);
        client.options.keyUse.setPressed(false);
        client.options.keyJump.setPressed(false);
        player.setPitch(0); // Look forwards
    }
}
