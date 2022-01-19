package net.advancedautopilot.pilot;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

/**
 * Defines helper methods that are out-of-scope for the Pilot class.
 */
public class PilotHelper {

    private static final int CHEST_SLOT = 6;
    private static final String ELYTRA_KEY = "item.minecraft.elytra";
    private static final String FIREWORK_ROCKET_KEY = "item.minecraft.firework_rocket";

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

    public static boolean swapElytra(MinecraftClient client, PlayerEntity player) {
        // Optimization: find the first elytra with sufficient durability
        ItemStack newElytra = null;
        int minDurability = 10;
        for (ItemStack itemStack : player.getInventory().main) {
            if (itemStack.getItem().getTranslationKey().equals(ELYTRA_KEY)) {
                int itemDurability = itemStack.getMaxDamage() - itemStack.getDamage();
                if (itemDurability >= minDurability) {
                    newElytra = itemStack;
                    break;
                }
            }
        }

        if (newElytra != null) {
            client.interactionManager.clickSlot(
                    player.playerScreenHandler.syncId,
                    CHEST_SLOT,
                    player.getInventory().main.indexOf(newElytra),
                    SlotActionType.SWAP,
                    player);
            player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA, 1.0F, 1.0F);
            player.sendMessage(
                    new TranslatableText("text.advancedautopilot.swappedElytra").formatted(Formatting.WHITE), true);
            return true;
        } else {
            return false;
        }
    }
}
