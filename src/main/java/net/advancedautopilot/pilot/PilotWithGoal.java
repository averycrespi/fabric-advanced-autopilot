package net.advancedautopilot.pilot;

import net.advancedautopilot.FlightMonitor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Represents a generic pilot with an optional goal.
 *
 * If the goal is not specified, the pilot should maintain its direction.
 */
public abstract class PilotWithGoal extends Pilot {

    private static final double ONE_RADIAN_IN_DEGREES = 57.2957763671875;

    private Vec3d goalPos = null;

    public PilotWithGoal(FlightMonitor monitor) {
        super(monitor);
    }

    public boolean hasGoal() {
        return goalPos != null;
    }

    public Vec3d getGoalPos() {
        return goalPos;
    }

    public void setGoalPos(Vec3d newGoalPos) {
        goalPos = newGoalPos;
    }

    public double getDistanceToGoal(PlayerEntity player) {
        if (goalPos == null) {
            return -1d;
        } else {
            Vec3d playerPos = player.getPos();
            return new Vec3d(goalPos.getX(), 0, goalPos.getZ())
                    .subtract(new Vec3d(playerPos.getX(), 0, playerPos.getY()))
                    .length();
        }
    }

    public void lookTowardsGoal(PlayerEntity player) {
        if (goalPos == null) {
            return;
        }

        Vec3d playerPos = player.getPos();
        double xDiff = (double) goalPos.x - playerPos.x;
        double yDiff = (double) goalPos.z - playerPos.z;
        float goalYaw = MathHelper
                .wrapDegrees((float) (MathHelper.atan2(yDiff, xDiff) * ONE_RADIAN_IN_DEGREES) - 90f);
        player.setYaw(goalYaw);
    }
}
