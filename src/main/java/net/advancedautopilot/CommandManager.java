package net.advancedautopilot;

import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;

/**
 * Manages the mod's commands.
 */
public class CommandManager {
    public static void register(AdvancedAutopilotMod mod, MinecraftClient client) {
        ClientCommandManager.DISPATCHER.register(
                ClientCommandManager.literal("land")
                        .executes(context -> {
                            PlayerEntity player = client.player;
                            if (player == null) {
                                return 1;
                            }

                            if (!player.isFallFlying()) {
                                player.sendMessage(
                                        new TranslatableText("text.advancedautopilot.notFlying")
                                                .formatted(AdvancedAutopilotMod.FAILURE),
                                        true);
                                return 1;
                            }

                            mod.land();
                            return 1;
                        }));
    }
}