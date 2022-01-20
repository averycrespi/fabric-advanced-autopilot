package net.advancedautopilot;

import net.advancedautopilot.pilot.AscendingPilot;
import net.advancedautopilot.pilot.GlidingPilot;
import net.advancedautopilot.pilot.LandingPilot;
import net.advancedautopilot.pilot.Pilot;
import net.advancedautopilot.pilot.PilotHelper;
import net.advancedautopilot.pilot.TickResult;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
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

    public static AdvancedAutopilotMod instance = null;

    public MinecraftClient client = null;
    public FlightMonitor monitor = null;
    public HudFormatter formatter = null;

    private Pilot pilot = null;
    private KeyBinding keyBinding = null;
    private boolean keyBindingWasPressedOnPreviousTick = false;
    private int ticksSincePreviousInfrequentTick = 0;
    private Vec3d goal = null;

    @Override
    public void onInitialize() {
        AdvancedAutopilotMod.instance = this;

        if (client == null) {
            client = MinecraftClient.getInstance();
        }

        if (monitor == null) {
            monitor = new FlightMonitor();
        }

        if (formatter == null) {
            formatter = new HudFormatter(monitor);
        }

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

        if (pilot != null && pilot.onClientTick(client, player, goal) == TickResult.YIELD) {
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
        monitor.onInfrequentClientTick(client, player, goal);
        formatter.onInfrequentClientTick(pilot, goal);

        if (pilot != null && (player.isTouchingWater() || player.isInLava())) {
            LOGGER.info("Disengaging autopilot because player touched liquid");
            pilot.cleanup(client, player);
            pilot = null;
        }

        if (pilot != null && pilot.onInfrequentClientTick(client, player, goal) == TickResult.YIELD) {
            handlePilotYield(player);
        }

        if (ConfigManager.currentConfig.swapElytra || ConfigManager.currentConfig.emergencyLanding) {
            monitorElytraDurability(player);
        }
    }

    private void monitorElytraDurability(PlayerEntity player) {
        if (pilot != null && PilotHelper.hasElytraEquipped(player)) {
            int elytraDurability = PilotHelper.getElytraDurability(player);

            if (ConfigManager.currentConfig.swapElytra && ConfigManager.currentConfig.emergencyLanding) {
                if (PilotHelper.canSwapElytra(player)) {
                    if (elytraDurability <= ConfigManager.currentConfig.maxElytraSwapDurability) {
                        if (!PilotHelper.swapElytra(client, player)) {
                            LOGGER.warn("Failed to swap elytra despite pre-check; attempting dangerous landing");
                            performEmergencyLanding(player);
                        }
                    }
                } else if (elytraDurability <= ConfigManager.currentConfig.maxEmergencyLandingDurability) {
                    performEmergencyLanding(player);
                }
            } else if (ConfigManager.currentConfig.swapElytra) {
                if (elytraDurability <= ConfigManager.currentConfig.maxElytraSwapDurability) {
                    PilotHelper.swapElytra(client, player); // Ignore swap failure
                }
            } else if (ConfigManager.currentConfig.emergencyLanding) {
                if (elytraDurability <= ConfigManager.currentConfig.maxEmergencyLandingDurability) {
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
        if (pilot != null) {
            pilot.cleanup(client, player);
        }

        if (player.isFallFlying()) {
            double height = monitor.getHeight();
            if (goal != null && monitor.getHorizontalDistanceToGoal() < 20) {
                pilot = new LandingPilot(monitor);
            } else if (height >= ConfigManager.currentConfig.ascentHeight) {
                pilot = new GlidingPilot(monitor);
            } else if (PilotHelper.isHoldingFirework(player)) {
                pilot = new AscendingPilot(monitor);
            } else {
                pilot = new LandingPilot(monitor);
            }
        } else {
            pilot = null;
            goal = null;
        }
    }

    private void handleKeyBindingPress(PlayerEntity player) {
        LOGGER.info("Keybinding was pressed");
        if (player.isFallFlying()) {
            if (pilot == null) {
                player.sendMessage(
                        new TranslatableText("text.advancedautopilot.engagedAutopilot").formatted(SUCCESS),
                        true);
                pilot = new AscendingPilot(monitor);
            } else {
                player.sendMessage(
                        new TranslatableText("text.advancedautopilot.disengagedAutopilot").formatted(SUCCESS),
                        true);
                pilot.cleanup(client, player);
                pilot = null;
                goal = null;
            }
        } else {
            player.sendMessage(new TranslatableText("text.advancedautopilot.notFlying").formatted(FAILURE), true);
        }
    }
}
