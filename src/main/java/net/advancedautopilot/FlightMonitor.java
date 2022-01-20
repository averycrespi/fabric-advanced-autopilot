package net.advancedautopilot;

import java.util.ArrayList;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Monitors flight metrics.
 */
public class FlightMonitor {

    public static final int CLIENT_TICKS_PER_SECOND = 20;

    private static final int MAX_PAST_VELOCITIES = 5 * CLIENT_TICKS_PER_SECOND;

    private double height = -1d;
    private Vec3d velocity = new Vec3d(0d, 0d, 0d);
    private Vec3d previousPos = new Vec3d(0d, 0d, 0d);
    private double horizontalDistanceToGoal = Double.POSITIVE_INFINITY;

    private int pastVelocityIndex = 0;
    private ArrayList<Vec3d> pastVelocities = new ArrayList<>();
    private ArrayList<Vec3d> pastHorizontalVelocities = new ArrayList<>();
    private double averageSpeed = 0d;
    private double averageHorizontalSpeed = 0d;

    // The approximate pitch and yaw are tracked for the benefit of the HUD
    // formatter; pilots should call player.getPitch() or player.getYaw() directly
    private double approxPitch = 0d;
    private double approxYaw = 0d;

    public void onClientTick(MinecraftClient client, PlayerEntity player) {
        updateVelocity(player, CLIENT_TICKS_PER_SECOND);
        updatePastVelocities();
    }

    public void onInfrequentClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal) {
        updateHeight(player);
        updateHorizontalDistanceToGoal(player, goal);
        updateAverageSpeeds();
        approxPitch = MathHelper.wrapDegrees((float) player.getPitch());
        approxYaw = MathHelper.wrapDegrees((float) player.getYaw());
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

    public double getHorizontalDistanceToGoal() {
        return horizontalDistanceToGoal;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public double getAverageHorizontalSpeed() {
        return averageHorizontalSpeed;
    }

    public double getApproxPitch() {
        return approxPitch;
    }

    public double getApproxYaw() {
        return approxYaw;
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

    private void updatePastVelocities() {
        Vec3d horizontalVelocity = velocity.subtract(new Vec3d(0, velocity.getY(), 0));
        if (pastVelocities.size() < MAX_PAST_VELOCITIES) {
            pastVelocities.add(velocity);
            pastHorizontalVelocities.add(horizontalVelocity);
        } else {
            pastVelocities.set(pastVelocityIndex, velocity);
            pastHorizontalVelocities.set(pastVelocityIndex, horizontalVelocity);
        }

        pastVelocityIndex = (pastVelocityIndex + 1) % MAX_PAST_VELOCITIES;
    }

    private void updateAverageSpeeds() {
        averageSpeed = pastVelocities
                .stream().mapToDouble(val -> val.length()).average().orElse(0d);
        averageHorizontalSpeed = pastHorizontalVelocities
                .stream().mapToDouble(val -> val.length()).average().orElse(0d);
    }

    private void updateHorizontalDistanceToGoal(PlayerEntity player, Vec3d goal) {
        if (goal == null) {
            horizontalDistanceToGoal = Double.POSITIVE_INFINITY;
            return;
        }

        Vec3d playerPos = player.getPos();
        horizontalDistanceToGoal = new Vec3d(goal.getX(), 0, goal.getZ())
                .subtract(new Vec3d(playerPos.getX(), 0, playerPos.getZ()))
                .length();
    }
}
