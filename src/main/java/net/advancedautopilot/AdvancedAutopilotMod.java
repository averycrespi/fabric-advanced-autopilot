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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class AdvancedAutopilotMod implements ModInitializer {

	public static final Logger LOGGER = LogManager.getLogger("advancedautopilot");

	public static AdvancedAutopilotMod instance = null;

	public MinecraftClient client = null;
	public FlightMonitor monitor = null;
	public HudFormatter formatter = null;

	private Pilot pilot = null;
	private KeyBinding keyBinding = null;
	private boolean keyBindingWasPressedOnPreviousTick = false;
	private int ticksSincePreviousInfrequentTick = 0;

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

	public void land() {
		PlayerEntity player = client.player;
		if (player == null) {
			return;
		}

		if (!player.isFallFlying()) {
			return;
		}

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

		if (pilot != null && pilot.onClientTick(client, player) == TickResult.YIELD) {
			handlePilotYield(player);
		}

		if (ticksSincePreviousInfrequentTick >= 20) {
			monitor.onInfrequentClientTick(client, player);
			formatter.onInfrequentClientTick(pilot);
			if (pilot != null && pilot.onInfrequentClientTick(client, player) == TickResult.YIELD) {
				handlePilotYield(player);
			}
			ticksSincePreviousInfrequentTick = 0;
		}

		if (!keyBindingWasPressedOnPreviousTick && keyBinding.isPressed()) {
			handleKeyBindingPress(player);
		}
		keyBindingWasPressedOnPreviousTick = keyBinding.isPressed();
	}

	private void handlePilotYield(PlayerEntity player) {
		pilot.reset(client, player);
		pilot = null;

		if (player.isFallFlying()) {
			double height = monitor.getHeight();
			if (height >= ConfigManager.currentConfig.minHeightToStartGliding) {
				pilot = new GlidingPilot(monitor);
			} else if (PilotHelper.isHoldingFirework(player)) {
				pilot = new AscendingPilot(monitor);
			} else {
				pilot = new LandingPilot(monitor);
			}
		}
	}

	private void handleKeyBindingPress(PlayerEntity player) {
		LOGGER.info("Keybinding was pressed");
		if (player.isFallFlying()) {
			if (pilot == null) {
				player.sendMessage(
						new TranslatableText("text.advancedautopilot.autopilot.engaged").formatted(Formatting.GREEN),
						true);
				pilot = new AscendingPilot(monitor);
			} else {
				player.sendMessage(
						new TranslatableText("text.advancedautopilot.autopilot.disengaged")
								.formatted(Formatting.YELLOW),
						true);
				pilot.reset(client, player);
				pilot = null;
			}
		} else {
			client.setScreen(ConfigManager.createConfigScreen(client.currentScreen));
		}
	}
}
