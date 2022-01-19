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

    public void onInfrequentClientTick(Pilot pilot) {
        lines.clear();

        lines.add(new TranslatableText("text.advancedautopilot.pilot",
                pilot == null ? "None" : pilot.getClass().getSimpleName()));

        lines.add(new LiteralText(""));

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

    public ArrayList<Text> getLines() {
        return lines;
    }
}
