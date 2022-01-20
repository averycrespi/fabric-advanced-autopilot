package net.advancedautopilot;

import java.util.ArrayList;

import net.advancedautopilot.pilot.Pilot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Vec3d;

/**
 * Formats the contents of the HUD.
 */
public class HudFormatter {

    private FlightMonitor monitor = null;
    private ArrayList<Text> lines = null;

    public HudFormatter(FlightMonitor monitor) {
        this.monitor = monitor;
        lines = new ArrayList<>();
    }

    public ArrayList<Text> getLines() {
        return lines;
    }

    public void onInfrequentClientTick(Pilot pilot, Vec3d goal) {
        lines.clear();

        addPilot(pilot);
        addSpacer();
        addMetrics();

        if (goal != null) {
            addSpacer();
            addGoal(goal);
        }

        if (ConfigManager.currentConfig.debug) {
            addSpacer();
            addPitchAndYaw();
            addSpacer();
            addOptions();
        }
    }

    private void addSpacer() {
        lines.add(new LiteralText(""));
    }

    private void addPilot(Pilot pilot) {
        lines.add(new TranslatableText("text.advancedautopilot.pilot",
                pilot == null ? "None" : pilot.getClass().getSimpleName()));
    }

    private void addMetrics() {
        lines.add(new TranslatableText(
                "text.advancedautopilot.speed",
                String.format("%.2f", monitor.getSpeed())));

        lines.add(new TranslatableText(
                "text.advancedautopilot.height",
                String.format("%d", (long) monitor.getHeight())));

        Vec3d playerPos = monitor.getPosition();
        lines.add(new TranslatableText(
                "text.advancedautopilot.position",
                String.format("%d", (long) playerPos.getX()),
                String.format("%d", (long) playerPos.getY()),
                String.format("%d", (long) playerPos.getZ())));
    }

    private void addGoal(Vec3d goal) {
        lines.add(new TranslatableText("text.advancedautopilot.goal",
                String.format("%d", (long) goal.getX()),
                String.format("%d", (long) goal.getZ())));
        lines.add(new TranslatableText("text.advancedautopilot.distanceToGoal",
                String.format("%.2f", monitor.getHorizontalDistanceToGoal())));
    }

    private void addPitchAndYaw() {
        lines.add(new TranslatableText("text.advancedautopilot.pitch",
                String.format("%.2f", monitor.getApproxPitch())));
        lines.add(new TranslatableText("text.advancedautopilot.yaw",
                String.format("%.2f", monitor.getApproxYaw())));
    }

    private void addOptions() {
        lines.add((Text) new TranslatableText("text.advancedautopilot.swapElytra").append(
                ConfigManager.currentConfig.swapElytra
                        ? new TranslatableText("text.advancedautopilot.enabled")
                        : new TranslatableText("text.advancedautopilot.disabled")));
        lines.add((Text) new TranslatableText("text.advancedautopilot.emergencyLanding").append(
                ConfigManager.currentConfig.emergencyLanding
                        ? new TranslatableText("text.advancedautopilot.enabled")
                        : new TranslatableText("text.advancedautopilot.disabled")));
    }
}
