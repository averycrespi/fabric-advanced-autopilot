package net.advancedautopilot.pilots;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.advancedautopilot.AdvancedAutopilotConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Represents a generic pilot.
 */
public abstract class Pilot {
    public static final Logger LOGGER = LogManager.getLogger("advancedautopilot.Pilot");

    public static final double CLIENT_TICKS_PER_SECOND = 20d;
    public static final double UNKNOWN_DISTANCE_TO_GROUND = -1d;

    private double distanceToGround = UNKNOWN_DISTANCE_TO_GROUND;
    private Vec3d velocity = null;
    private Vec3d previousPos = null;

    /**
     * Should be called once every screen tick (frame) while the pilot is active.
     */
    public abstract TickResult onScreenTick(MinecraftClient client, PlayerEntity player,
            AdvancedAutopilotConfig config);

    /**
     * Should be called once every client tick while the pilot is active.
     */
    public abstract TickResult onClientTick(MinecraftClient client, PlayerEntity player,
            AdvancedAutopilotConfig config);

    /**
     * Should be called once every ~20 client ticks while the pilot is active.
     */
    public abstract TickResult onInfrequentClientTick(MinecraftClient client, PlayerEntity player,
            AdvancedAutopilotConfig config);

    /**
     * Reset the pilot, restoring all variables to their defaults.
     *
     * If you override this method, make sure that you call super.reset().
     */
    public void reset(MinecraftClient client, PlayerEntity player) {
        distanceToGround = UNKNOWN_DISTANCE_TO_GROUND;
        velocity = null;
        previousPos = null;
    };

    public double getDistanceToGround() {
        return distanceToGround;
    }

    public void updateDistanceToGround(PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        double playerX = (double) playerPos.getX();
        double playerY = (double) playerPos.getY();
        double playerZ = (double) playerPos.getZ();

        World world = player.world;
        double bottomY = (double) world.getBottomY();

        // Search downwards from the player until we find a solid block
        distanceToGround = UNKNOWN_DISTANCE_TO_GROUND;
        if (player.world.isChunkLoaded((int) playerX, (int) playerZ)) {
            for (double y = playerY; y > bottomY; y--) {
                BlockPos blockPos = new BlockPos(playerX, y, playerZ);
                if (world.getBlockState(blockPos).isSolidBlock(world, blockPos)) {
                    distanceToGround = playerY - y;
                    return;
                }
            }
        }
    }

    public Vec3d getVelocity() {
        return velocity;
    }

    public void updateVelocity(PlayerEntity player, double callsPerSecond) {
        Vec3d playerPos = player.getPos();
        if (previousPos == null) {
            previousPos = playerPos;
        }

        velocity = playerPos.subtract(previousPos).multiply(callsPerSecond);
        previousPos = playerPos;
    }
}
