package net.advancedautopilot;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Monitors flight metrics.
 */
public class FlightMonitor {

    public static final double CLIENT_TICKS_PER_SECOND = 20d;

    private double height = -1d;
    private Vec3d velocity = new Vec3d(0d, 0d, 0d);
    private Vec3d previousPos = new Vec3d(0d, 0d, 0d);

    public void onClientTick(MinecraftClient client, PlayerEntity player) {
        updateVelocity(player, CLIENT_TICKS_PER_SECOND);
    }

    public void onInfrequentClientTick(MinecraftClient client, PlayerEntity player) {
        updateHeight(player);
    }

    public double getHeight() {
        return height;
    }

    public Vec3d getVelocity() {
        return velocity;
    }

    public double getSpeed() {
        return velocity.length();
    }

    public Vec3d getPosition() {
        return previousPos;
    }

    private void updateHeight(PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        double playerX = (double) playerPos.getX();
        double playerY = (double) playerPos.getY();
        double playerZ = (double) playerPos.getZ();

        World world = player.world;
        double bottomY = (double) world.getBottomY();

        // Search downwards from the player until we find a solid block
        height = -1d;
        if (player.world.isChunkLoaded((int) playerX, (int) playerZ)) {
            for (double y = playerY; y > bottomY; y--) {
                BlockPos blockPos = new BlockPos(playerX, y, playerZ);
                if (world.getBlockState(blockPos).isSolidBlock(world, blockPos)) {
                    height = playerY - y;
                    return;
                }
            }
        }
    }

    private void updateVelocity(PlayerEntity player, double callsPerSecond) {
        Vec3d playerPos = player.getPos();
        velocity = playerPos.subtract(previousPos).multiply(callsPerSecond);
        previousPos = playerPos;
    }
}
