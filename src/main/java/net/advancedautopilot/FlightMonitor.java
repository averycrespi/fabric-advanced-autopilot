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

    private static final int CLIENT_TICKS_PER_SECOND = 20;
    private static final int MAX_PAST_VELOCITIES = 30;
    private static final double UNKNOWN_HEIGHT = -1d;
    private static final double UNKNOWN_DISTANCE = 1e9d;
    private static final double UNKNOWN_ETA = 1e9d;

    private double height = UNKNOWN_HEIGHT;
    private Vec3d velocity = new Vec3d(0d, 0d, 0d);
    private Vec3d previousPos = new Vec3d(0d, 0d, 0d);
    private double horizontalDistanceToGoal = UNKNOWN_DISTANCE;

    private int pastSpeedIndex = 0;
    private ArrayList<Double> pastSpeeds = new ArrayList<>();
    private ArrayList<Double> pastHorizontalSpeeds = new ArrayList<>();
    private double averageSpeed = 0d;
    private double averageHorizontalSpeed = 0d;
    private double eta = UNKNOWN_ETA;

    private int timeInUnloadedChunks = 0;

    // The approximate pitch and yaw are tracked for the benefit of the HUD
    // formatter; pilots should call player.getPitch() or player.getYaw() directly
    private double approxPitch = 0d;
    private double approxYaw = 0d;

    public void onClientTick(MinecraftClient client, PlayerEntity player) {
        updateVelocity(player, CLIENT_TICKS_PER_SECOND);
    }

    public void onInfrequentClientTick(MinecraftClient client, PlayerEntity player, Vec3d goal) {
        updateHeightAndTimeInUnloadedChunks(player);
        updateHorizontalDistanceToGoal(player, goal);
        updatePastVelocities();
        updateAverageSpeeds();
        updateEta(goal);

        approxPitch = MathHelper.wrapDegrees((float) player.getPitch());
        approxYaw = MathHelper.wrapDegrees((float) player.getYaw());
    }

    public void resetAggregateMetrics() {
        pastSpeedIndex = 0;
        pastSpeeds.clear();
        pastHorizontalSpeeds.clear();
        averageSpeed = 0d;
        averageHorizontalSpeed = 0d;
        eta = UNKNOWN_ETA;
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

    public double getEta() {
        return eta;
    }

    public int getTimeInUnloadedChunks() {
        return timeInUnloadedChunks;
    }

    public double getApproxPitch() {
        return approxPitch;
    }

    public double getApproxYaw() {
        return approxYaw;
    }

    private void updateHeightAndTimeInUnloadedChunks(PlayerEntity player) {
        Config config = ConfigManager.getCurrentConfig();

        Vec3d playerPos = player.getPos();
        double playerX = (double) playerPos.getX();
        double playerY = (double) playerPos.getY();
        double playerZ = (double) playerPos.getZ();

        World world = player.world;
        double bottomY = (double) world.getBottomY();

        // Search downwards from the player until we find a solid block
        for (double y = playerY; y > bottomY; y--) {
            BlockPos blockPos = new BlockPos(playerX, y, playerZ);
            if (world.getBlockState(blockPos).isSolidBlock(world, blockPos)) {
                height = playerY - y;
                timeInUnloadedChunks = 0;
                return;
            }
        }

        timeInUnloadedChunks += 1;
        if (!config.allowUnloadedChunks) {
            height = UNKNOWN_HEIGHT;
        }
    }

    private void updateVelocity(PlayerEntity player, double callsPerSecond) {
        Vec3d playerPos = player.getPos();
        velocity = playerPos.subtract(previousPos).multiply(callsPerSecond);
        previousPos = playerPos;
    }

    private void updatePastVelocities() {
        Vec3d horizontalVelocity = velocity.subtract(new Vec3d(0, velocity.getY(), 0));
        if (pastSpeeds.size() < MAX_PAST_VELOCITIES) {
            pastSpeeds.add(velocity.length());
            pastHorizontalSpeeds.add(horizontalVelocity.length());
        } else {
            pastSpeeds.set(pastSpeedIndex, velocity.length());
            pastHorizontalSpeeds.set(pastSpeedIndex, horizontalVelocity.length());
        }

        pastSpeedIndex = (pastSpeedIndex + 1) % MAX_PAST_VELOCITIES;
    }

    private void updateAverageSpeeds() {
        averageSpeed = pastSpeeds
                .stream().mapToDouble(val -> val).average().orElse(0d);
        averageHorizontalSpeed = pastHorizontalSpeeds
                .stream().mapToDouble(val -> val).average().orElse(0d);
    }

    private void updateEta(Vec3d goal) {
        if (goal == null) {
            eta = UNKNOWN_ETA;
        } else if (averageHorizontalSpeed < 0.01) {
            eta = UNKNOWN_ETA;
        } else {
            eta = horizontalDistanceToGoal / averageHorizontalSpeed;
        }
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
