package net.advancedautopilot;

import net.advancedautopilot.pilot.AscendingPilot;
import net.advancedautopilot.pilot.GlidingPilot;
import net.advancedautopilot.pilot.LandingPilot;
import net.advancedautopilot.pilot.Pilot;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class AdvancedAutopilotMod implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("advancedautopilot");

    public static final Formatting SUCCESS = Formatting.GREEN;
    public static final Formatting FAILURE = Formatting.RED;
    public static final Formatting INFO = Formatting.WHITE;

    public static AdvancedAutopilotMod instance;

    public MinecraftClient client;
    public FlightMonitor monitor;
    public HudFormatter formatter;

    private Pilot pilot;
    private Vec3d goal;
    private KeyBinding keyBinding;
    private boolean keyBindingWasPressedOnPreviousTick = false;
    private int ticksSincePreviousInfrequentTick = 0;

    @Override
    public void onInitialize() {
        AdvancedAutopilotMod.instance = this;

        client = MinecraftClient.getInstance();
        monitor = new FlightMonitor();
        formatter = new HudFormatter(monitor);

        keyBinding = new KeyBinding(
                "key.advancedautopilot.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "title.advancedautopilot");
        KeyBindingHelper.registerKeyBinding(keyBinding);

        ClientTickEvents.END_CLIENT_TICK.register(e -> this.onClientTick());

        ConfigManager.initialize();
        CommandManager.register(AdvancedAutopilotMod.instance, client);

        LOGGER.info("Initialized Advanced Autopilot mod");
    }

    public void setGoal(Vec3d goal) {
        this.goal = goal;
    }

    public void clearGoal() {
        goal = null;
    }

    public void land() {
        PlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        if (!player.isFallFlying()) {
            return;
        }

        if (pilot != null) {
            pilot.cleanup(client, player);
        }

        player.sendMessage(
                new TranslatableText("text.advancedautopilot.performingAutomaticLanding").formatted(SUCCESS),
                true);
        pilot = new LandingPilot(monitor);
    }

    private void onClientTick() {
        ticksSincePreviousInfrequentTick++;

        if (client.isPaused() && client.isInSingleplayer()) {
            return;
        }

        PlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        monitor.onClientTick(client, player);

        if (pilot != null && pilot.onClientTick(client, player, goal) == Pilot.TickResult.YIELD) {
            handlePilotYield(player);
        }

        if (ticksSincePreviousInfrequentTick >= 20) {
            onInfrequentClientTick(player);
            ticksSincePreviousInfrequentTick = 0;
        }

        if (!keyBindingWasPressedOnPreviousTick && keyBinding.isPressed()) {
            handleKeyBindingPress(player);
        }
        keyBindingWasPressedOnPreviousTick = keyBinding.isPressed();
    }

    private void onInfrequentClientTick(PlayerEntity player) {
        Config config = ConfigManager.getCurrentConfig();

        monitor.onInfrequentClientTick(client, player, goal);
        formatter.onInfrequentClientTick(pilot, goal);

        if (pilot != null && (player.isTouchingWater() || player.isInLava())) {
            disengageAutopilot(
                    client,
                    player,
                    new TranslatableText("text.advancedautopilot.disengagedAutopilot.touchedLiquid")
                            .formatted(FAILURE));
        }

        if (pilot != null && pilot.onInfrequentClientTick(client, player, goal) == Pilot.TickResult.YIELD) {
            handlePilotYield(player);
        }

        if (config.refillRockets) {
            monitorRockets(player);
        }

        if (config.swapElytra || config.emergencyLanding) {
            monitorElytraDurability(player);
        }
    }

    private void monitorRockets(PlayerEntity player) {
        if (pilot != null && !PilotHelper.isHoldingFireworkRocket(player)) {
            PilotHelper.swapFireworkRocketsIntoMainHand(client, player); // Ignore swap failure
        }
    }

    private void monitorElytraDurability(PlayerEntity player) {
        Config config = ConfigManager.getCurrentConfig();

        if (pilot != null && PilotHelper.hasElytraEquipped(player)) {
            int elytraDurability = PilotHelper.getElytraDurability(player);

            if (config.swapElytra && config.emergencyLanding) {
                if (PilotHelper.canSwapElytra(player)) {
                    if (elytraDurability <= config.maxElytraSwapDurability) {
                        if (!PilotHelper.swapElytra(client, player)) {
                            LOGGER.warn("Failed to swap elytra despite pre-check; attempting dangerous landing");
                            performEmergencyLanding(player);
                        }
                    }
                } else if (elytraDurability <= config.maxEmergencyLandingDurability) {
                    performEmergencyLanding(player);
                }
            } else if (config.swapElytra) {
                if (elytraDurability <= config.maxElytraSwapDurability) {
                    PilotHelper.swapElytra(client, player); // Ignore swap failure
                }
            } else if (config.emergencyLanding) {
                if (elytraDurability <= config.maxEmergencyLandingDurability) {
                    performEmergencyLanding(player);
                }
            }
        }
    }

    private void performEmergencyLanding(PlayerEntity player) {
        if (pilot != null) {
            pilot.cleanup(client, player);
        }

        player.sendMessage(
                new TranslatableText("text.advancedautopilot.performingEmergencyLanding").formatted(SUCCESS),
                true);
        pilot = new LandingPilot(monitor);
    }

    private void handlePilotYield(PlayerEntity player) {
        Config config = ConfigManager.getCurrentConfig();

        if (pilot != null) {
            pilot.cleanup(client, player);
        }

        if (player.isFallFlying()) {
            double height = monitor.getHeight();
            if (goal != null && monitor.getHorizontalDistanceToGoal() < 20) {
                pilot = new LandingPilot(monitor);
            } else if (height >= config.ascentHeight) {
                pilot = new GlidingPilot(monitor);
            } else if (PilotHelper.isHoldingFireworkRocket(player)) {
                pilot = new AscendingPilot(monitor);
            } else {
                pilot = new LandingPilot(monitor);
            }
        } else {
            disengageAutopilot(
                    client,
                    player,
                    new TranslatableText("text.advancedautopilot.disengagedAutopilot.notFlying").formatted(FAILURE));
        }
    }

    private void handleKeyBindingPress(PlayerEntity player) {
        LOGGER.info("Keybinding was pressed");
        if (player.isFallFlying()) {
            if (pilot == null) {
                engageAutopilot(player);
            } else {
                disengageAutopilot(
                        client,
                        player,
                        new TranslatableText("text.advancedautopilot.disengagedAutopilot").formatted(INFO));
            }
        } else {
            player.sendMessage(new TranslatableText("text.advancedautopilot.notFlying").formatted(FAILURE), true);
        }
    }

    private void engageAutopilot(PlayerEntity player) {
        player.sendMessage(
                new TranslatableText("text.advancedautopilot.engagedAutopilot").formatted(SUCCESS),
                true);
        pilot = new AscendingPilot(monitor);
        monitor.resetAggregateMetrics();
    }

    private void disengageAutopilot(MinecraftClient client, PlayerEntity player, Text message) {
        if (message != null) {
            player.sendMessage(message, true);
        }

        if (pilot != null) {
            pilot.cleanup(client, player);
            pilot = null;
        }

        goal = null;
        monitor.resetAggregateMetrics();
    }
}
