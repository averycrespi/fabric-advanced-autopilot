package net.advancedautopilot;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public final class CommandManager {
    private static final int MIN_COORD = -2000000000;
    private static final int MAX_COORD = 2000000000;

    private CommandManager() {
        // Intentionally left empty.
    }

    public static void register(AdvancedAutopilotMod mod, MinecraftClient client) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    literal("setgoal")
                            .then(argument("X", IntegerArgumentType.integer(MIN_COORD, MAX_COORD))
                                    .then(argument("Z", IntegerArgumentType.integer(MIN_COORD, MAX_COORD))
                                            .executes(context -> {
                                                int goalX = IntegerArgumentType.getInteger(context, "X");
                                                int goalZ = IntegerArgumentType.getInteger(context, "Z");
                                                Vec3d goal = new Vec3d((double) goalX, 0, (double) goalZ);
                                                mod.setGoal(goal);
                                                context.getSource().sendFeedback(
                                                        Text.translatable("text.advancedautopilot.setGoal", goalX,
                                                                goalZ)
                                                                .formatted(AdvancedAutopilotMod.SUCCESS));
                                                return -1;
                                            }))));

            dispatcher.register(literal("cleargoal")
                    .executes(context -> {
                        mod.clearGoal();
                        context.getSource().sendFeedback(
                                Text.translatable("text.advancedautopilot.clearedGoal")
                                        .formatted(AdvancedAutopilotMod.SUCCESS));
                        return 1;
                    }));

            dispatcher.register(literal("land")
                    .executes(context -> {
                        PlayerEntity player = client.player;
                        if (player == null) {
                            return 1;
                        }

                        if (!player.isFallFlying()) {
                            player.sendMessage(
                                    Text.translatable("text.advancedautopilot.notFlying")
                                            .formatted(AdvancedAutopilotMod.FAILURE),
                                    true);
                            return 1;
                        }

                        mod.land();
                        return 1;
                    }));
        });
    }
}