package net.advancedautopilot;

import java.util.ArrayList;

import net.advancedautopilot.pilot.Pilot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

/**
 * Formats the contents of the HUD.
 */
public class HudFormatter {

    private static final Text ENABLED = Text.translatable("text.advancedautopilot.enabled")
            .formatted(Formatting.GREEN);
    private static final Text DISABLED = Text.translatable("text.advancedautopilot.disabled")
            .formatted(Formatting.RED);

    private FlightMonitor monitor;
    private ArrayList<Text> lines;

    public HudFormatter(FlightMonitor monitor) {
        this.monitor = monitor;
        lines = new ArrayList<>();
    }

    public ArrayList<Text> getLines() {
        return lines;
    }

    public void onInfrequentClientTick(Pilot pilot, Vec3d goal) {
        Config config = ConfigManager.getCurrentConfig();

        lines.clear();

        addPilot(pilot);
        if (config.showPilotStateInHud) {
            addPilotState(pilot);
        }

        addSpacer();
        addMetrics();

        if (goal != null) {
            addSpacer();
            addGoalAndEta(goal);
        }

        if (config.showAverageSpeedInHud) {
            addSpacer();
            addAverageSpeed();
        }

        if (config.showAnglesInHud) {
            addSpacer();
            addAngles();
        }

        if (config.showConfigOptionsInHud) {
            addSpacer();
            addConfigOptions();
        }
    }

    private void addSpacer() {
        lines.add(Text.of(""));
    }

    private void addPilot(Pilot pilot) {
        lines.add((Text) Text.translatable("text.advancedautopilot.pilot")
                .append(pilot == null
                        ? Text.translatable("text.advancedautopilot.noPilot")
                        : pilot.getName()));
    }

    private void addPilotState(Pilot pilot) {
        lines.add((Text) Text.translatable("text.advancedautopilot.pilotState")
                .append(pilot == null
                        ? Text.translatable("text.advancedautopilot.noPilotState")
                        : pilot.getState()));
    }

    private void addMetrics() {
        lines.add(Text.translatable(
                "text.advancedautopilot.speed",
                String.format("%.2f", monitor.getSpeed())));

        lines.add(Text.translatable(
                "text.advancedautopilot.height",
                String.format("%d", (long) monitor.getHeight())));

        Vec3d playerPos = monitor.getPosition();
        lines.add(Text.translatable(
                "text.advancedautopilot.position",
                String.format("%d", (long) playerPos.getX()),
                String.format("%d", (long) playerPos.getY()),
                String.format("%d", (long) playerPos.getZ())));
    }

    private void addGoalAndEta(Vec3d goal) {
        lines.add(Text.translatable("text.advancedautopilot.goal",
                String.format("%d", (long) goal.getX()),
                String.format("%d", (long) goal.getZ())));
        lines.add(Text.translatable("text.advancedautopilot.distanceToGoal",
                String.format("%d", (long) monitor.getHorizontalDistanceToGoal())));
        lines.add(Text.translatable("text.advancedautopilot.eta",
                String.format("%d", (long) monitor.getEta())));
    }

    public void addAverageSpeed() {
        lines.add(Text.translatable(
                "text.advancedautopilot.averageSpeed",
                String.format("%.2f", monitor.getAverageSpeed())));

        lines.add(Text.translatable(
                "text.advancedautopilot.averageHorizontalSpeed",
                String.format("%.2f", monitor.getAverageHorizontalSpeed())));
    }

    private void addAngles() {
        lines.add(Text.translatable("text.advancedautopilot.pitch",
                String.format("%.2f", monitor.getApproxPitch())));
        lines.add(Text.translatable("text.advancedautopilot.yaw",
                String.format("%.2f", monitor.getApproxYaw())));
    }

    private void addConfigOptions() {
        Config config = ConfigManager.getCurrentConfig();

        lines.add((Text) Text.translatable("text.advancedautopilot.swapElytra").append(
                config.swapElytra ? ENABLED : DISABLED));
        lines.add((Text) Text.translatable("text.advancedautopilot.emergencyLanding").append(
                config.emergencyLanding ? ENABLED : DISABLED));
        lines.add((Text) Text.translatable("text.advancedautopilot.poweredFlight").append(
                config.poweredFlight ? ENABLED : DISABLED));
        lines.add((Text) Text.translatable("text.advancedautopilot.refillRockets").append(
                config.refillRockets ? ENABLED : DISABLED));
        lines.add((Text) Text.translatable("text.advancedautopilot.riskyLanding").append(
                config.riskyLanding ? ENABLED : DISABLED));
        lines.add((Text) Text.translatable("text.advancedautopilot.allowUnloadedChunks").append(
                config.allowUnloadedChunks ? ENABLED : DISABLED));
        lines.add((Text) Text.translatable("text.advancedautopilot.resumeFlightTowardsGoal").append(
                config.resumeFlightTowardsGoal ? ENABLED : DISABLED));
    }
}
