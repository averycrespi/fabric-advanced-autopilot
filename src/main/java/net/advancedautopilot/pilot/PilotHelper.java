package net.advancedautopilot.pilot;

import net.advancedautopilot.AdvancedAutopilotMod;
import net.advancedautopilot.Config;
import net.advancedautopilot.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Defines helper methods that are out-of-scope for the Pilot class.
 */
public class PilotHelper {

    private static final int CHEST_SLOT = 6;
    private static final String ELYTRA_KEY = "item.minecraft.elytra";
    private static final String FIREWORK_ROCKET_KEY = "item.minecraft.firework_rocket";
    private static final double ONE_RADIAN_IN_DEGREES = 57.2957763671875;

    public static float getGoalYaw(PlayerEntity player, Vec3d goal) {
        if (goal == null) {
            return player.getYaw();
        }

        Vec3d playerPos = player.getPos();
        double xDiff = (double) goal.x - playerPos.x;
        double yDiff = (double) goal.z - playerPos.z;
        float goalYaw = MathHelper
                .wrapDegrees((float) (MathHelper.atan2(yDiff, xDiff) * ONE_RADIAN_IN_DEGREES) - 90f);
        return goalYaw;
    }

    public static boolean isHoldingFirework(PlayerEntity player) {
        Item mainHandItem = player.getMainHandStack().getItem();
        Item offHandItem = player.getOffHandStack().getItem();
        return mainHandItem.getTranslationKey().equals(FIREWORK_ROCKET_KEY)
                || offHandItem.getTranslationKey().equals(FIREWORK_ROCKET_KEY);
    }

    public static boolean hasElytraEquipped(PlayerEntity player) {
        Item chestItem = player.getInventory().armor.get(2).getItem();
        return chestItem.getTranslationKey().equals(ELYTRA_KEY);
    }

    public static int getElytraDurability(PlayerEntity player) {
        return player.getInventory().armor.get(2).getMaxDamage()
                - player.getInventory().armor.get(2).getDamage();
    }

    public static boolean canSwapElytra(PlayerEntity player) {
        Config config = ConfigManager.getCurrentConfig();

        // Optimization: look for the first viable replacement, not the best
        for (ItemStack itemStack : player.getInventory().main) {
            if (itemStack.getItem().getTranslationKey().equals(ELYTRA_KEY)) {
                int itemDurability = itemStack.getMaxDamage() - itemStack.getDamage();
                if (itemDurability >= config.minElytraSwapReplacementDurability) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean swapElytra(MinecraftClient client, PlayerEntity player) {
        ItemStack replacementElytra = findReplacementElytra(player);
        if (replacementElytra != null) {
            client.interactionManager.clickSlot(
                    player.playerScreenHandler.syncId,
                    CHEST_SLOT,
                    player.getInventory().main.indexOf(replacementElytra),
                    SlotActionType.SWAP,
                    player);
            player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA, 1.0F, 1.0F);
            player.sendMessage(
                    new TranslatableText("text.advancedautopilot.swappedElytra").formatted(AdvancedAutopilotMod.INFO),
                    true);
            return true;
        } else {
            return false;
        }
    }

    private static ItemStack findReplacementElytra(PlayerEntity player) {
        Config config = ConfigManager.getCurrentConfig();

        ItemStack replacementElytra = null;
        int bestDurability = config.minElytraSwapReplacementDurability;
        for (ItemStack itemStack : player.getInventory().main) {
            if (itemStack.getItem().getTranslationKey().equals(ELYTRA_KEY)) {
                int itemDurability = itemStack.getMaxDamage() - itemStack.getDamage();
                if (itemDurability >= bestDurability) {
                    replacementElytra = itemStack;
                    bestDurability = itemDurability;
                    break;
                }
            }
        }
        return replacementElytra;
    }
}
