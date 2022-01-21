package net.advancedautopilot.mixin;

import net.advancedautopilot.AdvancedAutopilotMod;
import net.advancedautopilot.Config;
import net.advancedautopilot.ConfigManager;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class HudMixin {

    @Inject(at = @At(value = "RETURN"), method = "render")
    public void renderPost(MatrixStack matrixStack, float f, CallbackInfo ci) {
        if (!ci.isCancelled()) {
            AdvancedAutopilotMod mod = AdvancedAutopilotMod.instance;
            if (mod == null
                    || mod.client.options.debugEnabled
                    || mod.client.player == null
                    || !mod.client.player.isFallFlying()) {
                return;
            }

            Config config = ConfigManager.getCurrentConfig();
            float stringX = (float) config.hudTextWidth;
            float stringY = (float) config.hudTextHeight;

            ArrayList<Text> lines = mod.formatter.getLines();
            for (int i = 0; i < lines.size(); i++) {
                mod.client.textRenderer.drawWithShadow(
                        matrixStack,
                        lines.get(i),
                        stringX,
                        stringY + i * (mod.client.textRenderer.fontHeight + 1),
                        0xFFFFFF);

            }
        }
    }
}
