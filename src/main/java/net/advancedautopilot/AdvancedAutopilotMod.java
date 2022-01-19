package net.advancedautopilot;

import net.advancedautopilot.pilots.AscendingPilot;
import net.advancedautopilot.pilots.Pilot;
import net.advancedautopilot.pilots.TickResult;
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

	private MinecraftClient client = null;
	private AdvancedAutopilotConfig config = null;
	private Pilot pilot = null;
	private KeyBinding keyBinding = null;
	private boolean keyBindingWasPressedOnPreviousTick = false;
	private int ticksSincePreviousInfrequentTick = 0;

	@Override
	public void onInitialize() {
		if (client == null) {
			client = MinecraftClient.getInstance();
		}

		if (config == null) {
			config = new AdvancedAutopilotConfig();
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
		if (client.isPaused() && client.isInSingleplayer()) {
			return;
		}

		PlayerEntity player = client.player;
		if (player == null) {
			return;
		}

		if (pilot != null) {
			if (pilot.onScreenTick(client, player, config) == TickResult.YIELD) {
				pilot.reset(client, player);
				pilot = null;
			}
		}
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

		if (pilot != null) {
			if (pilot.onClientTick(client, player, config) == TickResult.YIELD) {
				pilot.reset(client, player);
				pilot = null;
			}
		}

		if (ticksSincePreviousInfrequentTick >= 20) {
			onInfrequentClientTick(player);
			ticksSincePreviousInfrequentTick = 0;
		}

		if (!keyBindingWasPressedOnPreviousTick && keyBinding.isPressed()) {
			handleKeyBindingPress(client, player);
		}
		keyBindingWasPressedOnPreviousTick = keyBinding.isPressed();
	}

	private void onInfrequentClientTick(PlayerEntity player) {
		if (pilot != null) {
			if (pilot.onInfrequentClientTick(client, player, config) == TickResult.YIELD) {
				pilot.reset(client, player);
				pilot = null;
			}
		}
	}

	private void handleKeyBindingPress(MinecraftClient client, PlayerEntity player) {
		LOGGER.info("Keybinding was pressed");
		if (pilot == null) {
			pilot = new AscendingPilot();
		} else {
			pilot.reset(client, player);
			pilot = null;
		}
	}
}
