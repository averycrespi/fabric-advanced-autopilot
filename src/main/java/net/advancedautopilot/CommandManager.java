package net.advancedautopilot;

import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.client.MinecraftClient;

/**
 * Manages the mod's commands.
 */
public class CommandManager {
    public static void register(AdvancedAutopilotMod mod, MinecraftClient client) {
        ClientCommandManager.DISPATCHER.register(
                ClientCommandManager.literal("land")
                        .executes(context -> {
                            mod.land();
                            return 1;
                        }));
    }
}