package net.advancedautopilot.pilots;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

/**
 * Defines helper methods that are out-of-scope for the Pilot class.
 */
public class PilotHelper {

    private static final String FIREWORK_ROCKET_KEY = "item.minecraft.firework_rocket";

    public static boolean isHoldingFirework(PlayerEntity player) {
        Item mainHandItem = player.getMainHandStack().getItem();
        Item offHandItem = player.getOffHandStack().getItem();
        return mainHandItem.getTranslationKey().equals(FIREWORK_ROCKET_KEY)
                || offHandItem.getTranslationKey().equals(FIREWORK_ROCKET_KEY);
    }
}
