package net.advancedautopilot;

import net.advancedautopilot.pilot.AscendingPilot;
import net.advancedautopilot.pilot.GlidingPilot;
import net.advancedautopilot.pilot.Pilot;
import net.advancedautopilot.pilot.TickResult;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class AdvancedAutopilotMod implements ModInitializer {

	public static final Logger LOGGER = LogManager.getLogger("advancedautopilot");

	public static AdvancedAutopilotMod instance = null;

	public MinecraftClient client = null;
	public AdvancedAutopilotConfig config = null;
	public FlightMonitor monitor = null;
	public HudManager hudManager = null;

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

		if (config == null) {
			config = new AdvancedAutopilotConfig();
		}

		if (monitor == null) {
			monitor = new FlightMonitor();
		}

		if (hudManager == null) {
			hudManager = new HudManager(monitor);
		}

		keyBinding = new KeyBinding(
				"key.advancedautopilot.toggle",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				"title.advancedautopilot");
		KeyBindingHelper.registerKeyBinding(keyBinding);

		HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> AdvancedAutopilotMod.this.onScreenTick());
		ClientTickEvents.END_CLIENT_TICK.register(e -> this.onClientTick());

		LOGGER.info("Initialized Advanced Autopilot mod");
	}

	private void onScreenTick() {
		// TODO: implement
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
			hudManager.onInfrequentClientTick(pilot);
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

		double height = monitor.getHeight();
		if (height >= config.minHeightToStartGliding) {
			pilot = new GlidingPilot(config, monitor);
		}
	}

	private void handleKeyBindingPress(PlayerEntity player) {
		LOGGER.info("Keybinding was pressed");
		if (pilot == null) {
			pilot = new AscendingPilot(config, monitor);
		} else {
			pilot.reset(client, player);
			pilot = null;
		}
	}
}
