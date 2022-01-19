package net.advancedautopilot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

/**
 * Manages the mod configuration.
 */
public class ConfigManager {

    public static AdvancedAutopilotConfig currentConfig = new AdvancedAutopilotConfig();

    public static void initialize() {
        ConfigManager.currentConfig = loadConfigFromFile(findConfigFile());
    }

    public static Screen createConfigScreen(Screen parentScreen) {
        File configFile = findConfigFile();
        AdvancedAutopilotConfig config = loadConfigFromFile(configFile);

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parentScreen)
                .setTitle(new TranslatableText("title.advancedautopilot.config"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory generalCategory = builder
                .getOrCreateCategory(new TranslatableText("category.advancedautopilot.general"));

        generalCategory.addEntry(entryBuilder
                .startBooleanToggle(new TranslatableText("option.advancedautopilot.swapElytra"), config.swapElytra)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> config.swapElytra = newValue)
                .build());

        generalCategory.addEntry(entryBuilder
                .startBooleanToggle(new TranslatableText("option.advancedautopilot.emergencyLanding"),
                        config.emergencyLanding)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> config.emergencyLanding = newValue)
                .setTooltip(new TranslatableText("tooltip.advancedautopilot.emergencyLanding"))
                .build());

        ConfigCategory ascendingCategory = builder
                .getOrCreateCategory(new TranslatableText("category.advancedautopilot.ascending"));

        ascendingCategory.addEntry(entryBuilder
                .startDoubleField(new TranslatableText("option.advancedautopilot.ascentHeight"), config.ascentHeight)
                .setDefaultValue(240d)
                .setSaveConsumer(newValue -> config.ascentHeight = newValue)
                .build());

        ConfigCategory glidingCategory = builder
                .getOrCreateCategory(new TranslatableText("category.advancedautopilot.gliding"));

        glidingCategory.addEntry(entryBuilder
                .startDoubleField(new TranslatableText("option.advancedautopilot.minHeightToStartGliding"),
                        config.minHeightToStartGliding)
                .setDefaultValue(180d)
                .setSaveConsumer(newValue -> config.minHeightToStartGliding = newValue)
                .build());

        glidingCategory.addEntry(entryBuilder
                .startDoubleField(new TranslatableText("option.advancedautopilot.maxHeightWhileGliding"),
                        config.maxHeightWhileGliding)
                .setDefaultValue(360d)
                .setSaveConsumer(newValue -> config.maxHeightWhileGliding = newValue)
                .build());

        glidingCategory.addEntry(entryBuilder
                .startDoubleField(new TranslatableText("option.advancedautopilot.minSpeedBeforePullingDown"),
                        config.minSpeedBeforePullingDown)
                .setDefaultValue(10d)
                .setSaveConsumer(newValue -> config.minSpeedBeforePullingDown = newValue)
                .build());

        glidingCategory.addEntry(entryBuilder
                .startDoubleField(new TranslatableText("option.advancedautopilot.maxSpeedBeforePullingUp"),
                        config.maxSpeedBeforePullingUp)
                .setDefaultValue(40d)
                .setSaveConsumer(newValue -> config.maxSpeedBeforePullingUp = newValue)
                .build());

        ConfigCategory landingCategory = builder
                .getOrCreateCategory(new TranslatableText("category.advancedautopilot.landing"));

        landingCategory.addEntry(entryBuilder
                .startDoubleField(new TranslatableText("option.advancedautopilot.landingPitch"),
                        config.landingPitch)
                .setDefaultValue(30d)
                .setSaveConsumer(newValue -> config.landingPitch = newValue)
                .build());

        landingCategory.addEntry(entryBuilder
                .startDoubleField(new TranslatableText("option.advancedautopilot.maxLandingSpeed"),
                        config.maxLandingSpeed)
                .setDefaultValue(5d)
                .setSaveConsumer(newValue -> config.maxLandingSpeed = newValue)
                .build());

        landingCategory.addEntry(entryBuilder
                .startIntField(new TranslatableText("option.advancedautopilot.emergencyLandingDurability"),
                        config.emergencyLandingDurability)
                .setDefaultValue(50)
                .setSaveConsumer(newValue -> config.emergencyLandingDurability = newValue)
                .build());

        builder.setSavingRunnable(() -> {
            saveConfigToFile(config, configFile);
            ConfigManager.currentConfig = config;
        });

        return builder.build();
    }

    private static File findConfigFile() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        File configFile = Paths.get(configDir.toString(), "advancedautopilot", "config.json").toFile();
        configFile.getParentFile().mkdirs();
        try {
            configFile.createNewFile();
        } catch (IOException e) {
            AdvancedAutopilotMod.LOGGER.error("Failed to create config file because: " + e.getMessage());
        }
        return configFile;
    }

    private static AdvancedAutopilotConfig loadConfigFromFile(File configFile) {
        Gson gson = new Gson();
        try {
            Reader reader = Files.newBufferedReader(configFile.toPath());
            AdvancedAutopilotConfig config = gson.fromJson(reader, AdvancedAutopilotConfig.class);
            if (config == null) {
                AdvancedAutopilotMod.LOGGER.warn("Config is null; falling back to default config");
                return new AdvancedAutopilotConfig();
            } else {
                return config;
            }
        } catch (IOException e) {
            AdvancedAutopilotMod.LOGGER.error("Failed to load config from file because: " + e.getMessage());
            AdvancedAutopilotMod.LOGGER.warn("Falling back to default config");
            return new AdvancedAutopilotConfig();
        }
    }

    private static void saveConfigToFile(AdvancedAutopilotConfig config, File configFile) {
        Gson gson = new Gson();
        String configString = gson.toJson(config);
        try {
            FileWriter writer = new FileWriter(configFile);
            writer.write(configString);
            writer.close();
            AdvancedAutopilotMod.LOGGER.info("Saved config to file");
        } catch (IOException e) {
            AdvancedAutopilotMod.LOGGER.error("Failed to save config to file because: " + e.getMessage());
        }
    }

}
