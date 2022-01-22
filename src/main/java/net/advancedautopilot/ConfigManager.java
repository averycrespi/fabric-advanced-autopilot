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
public final class ConfigManager {

    private static final int MIN_ELYTRA_DURABILITY = 1;
    private static final int MAX_ELYTRA_DURABILITY = 432;
    private static final double MIN_HEIGHT = 0d; // Cannot be negative
    private static final double MIN_SPEED = 0d; // Cannot be negative
    private static final double MIN_WRAPPED_ANGLE = -180d;
    private static final double MAX_WRAPPED_ANGLE = 180d;

    private static Config currentConfig = new Config();

    private ConfigManager() {
        // Intentionally left empty
    }

    public static void initialize() {
        ConfigManager.currentConfig = loadConfigFromFile(findConfigFile());
    }

    public static Config getCurrentConfig() {
        return currentConfig;
    }

    public static Screen createConfigScreen(Screen parentScreen) {
        File configFile = findConfigFile();
        Config config = loadConfigFromFile(configFile);

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parentScreen)
                .setTitle(new TranslatableText("title.advancedautopilot.config"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        addGeneralCategory(builder, entryBuilder, config);
        addHudCategory(builder, entryBuilder, config);
        addAscendingCategory(builder, entryBuilder, config);
        addGlidingCategory(builder, entryBuilder, config);
        addLandingCategory(builder, entryBuilder, config);

        builder.setSavingRunnable(() -> {
            saveConfigToFile(config, configFile);
            ConfigManager.currentConfig = config;
        });

        return builder.build();
    }

    private static void addGeneralCategory(
            ConfigBuilder builder,
            ConfigEntryBuilder entryBuilder,
            Config config) {

        ConfigCategory generalCategory = builder
                .getOrCreateCategory(new TranslatableText("category.advancedautopilot.general"));

        generalCategory.addEntry(entryBuilder
                .startBooleanToggle(
                        new TranslatableText("option.advancedautopilot.swapElytra"),
                        config.swapElytra)
                .setDefaultValue(Config.DEFAULT_SWAP_ELYTRA)
                .setSaveConsumer(newValue -> config.swapElytra = newValue)
                .build());

        generalCategory.addEntry(entryBuilder
                .startIntField(
                        new TranslatableText("option.advancedautopilot.maxElytraSwapDurability"),
                        config.maxElytraSwapDurability)
                .setDefaultValue(Config.DEFAULT_MAX_ELYTRA_SWAP_DURABILITY)
                .setMin(MIN_ELYTRA_DURABILITY)
                .setMax(MAX_ELYTRA_DURABILITY)
                .setSaveConsumer(newValue -> config.maxElytraSwapDurability = newValue)
                .build());

        generalCategory.addEntry(entryBuilder
                .startIntField(new TranslatableText(
                        "option.advancedautopilot.minElytraSwapReplacementDurability"),
                        config.minElytraSwapReplacementDurability)
                .setDefaultValue(Config.DEFAULT_MIN_ELYTRA_SWAP_REPLACEMENT_DURABILITY)
                .setMin(MIN_ELYTRA_DURABILITY)
                .setMax(MAX_ELYTRA_DURABILITY)
                .setSaveConsumer(newValue -> config.minElytraSwapReplacementDurability = newValue)
                .build());

        generalCategory.addEntry(entryBuilder
                .startBooleanToggle(
                        new TranslatableText("option.advancedautopilot.emergencyLanding"),
                        config.emergencyLanding)
                .setDefaultValue(Config.DEFAULT_EMERGENCY_LANDING)
                .setSaveConsumer(newValue -> config.emergencyLanding = newValue)
                .setTooltip(new TranslatableText("tooltip.advancedautopilot.emergencyLanding"))
                .build());

        generalCategory.addEntry(entryBuilder
                .startIntField(
                        new TranslatableText("option.advancedautopilot.maxEmergencyLandingDurability"),
                        config.maxEmergencyLandingDurability)
                .setDefaultValue(Config.DEFAULT_MAX_EMERGENCY_LANDING_DURABILITY)
                .setSaveConsumer(newValue -> config.maxEmergencyLandingDurability = newValue)
                .setMin(MIN_ELYTRA_DURABILITY)
                .setMax(MAX_ELYTRA_DURABILITY)
                .build());

        generalCategory.addEntry(entryBuilder
                .startBooleanToggle(
                        new TranslatableText("option.advancedautopilot.poweredFlight"),
                        config.poweredFlight)
                .setDefaultValue(Config.DEFAULT_POWERED_FLIGHT)
                .setSaveConsumer(newValue -> config.poweredFlight = newValue)
                .build());

        generalCategory.addEntry(entryBuilder
                .startDoubleField(
                        new TranslatableText("option.advancedautopilot.maxPoweredFlightSpeed"),
                        config.maxPoweredFlightSpeed)
                .setDefaultValue(Config.DEFAULT_MAX_POWERED_FLIGHT_SPEED)
                .setSaveConsumer(newValue -> config.maxPoweredFlightSpeed = newValue)
                .build());

        generalCategory.addEntry(entryBuilder
                .startBooleanToggle(
                        new TranslatableText("option.advancedautopilot.refillRockets"),
                        config.refillRockets)
                .setDefaultValue(Config.DEFAULT_REFILL_ROCKETS)
                .setSaveConsumer(newValue -> config.refillRockets = newValue)
                .build());
    }

    private static void addHudCategory(
            ConfigBuilder builder,
            ConfigEntryBuilder entryBuilder,
            Config config) {

        ConfigCategory hudCategory = builder
                .getOrCreateCategory(new TranslatableText("category.advancedautopilot.hud"));

        hudCategory.addEntry(entryBuilder
                .startBooleanToggle(
                        new TranslatableText("option.advancedautopilot.showAnglesInHud"),
                        config.showAnglesInHud)
                .setDefaultValue(Config.DEFAULT_SHOW_ANGLES_IN_HUD)
                .setSaveConsumer(newValue -> config.showAnglesInHud = newValue)
                .build());

        hudCategory.addEntry(entryBuilder
                .startBooleanToggle(
                        new TranslatableText("option.advancedautopilot.showAverageSpeedInHud"),
                        config.showAverageSpeedInHud)
                .setDefaultValue(Config.DEFAULT_SHOW_AVERAGE_SPEED_IN_HUD)
                .setSaveConsumer(newValue -> config.showAverageSpeedInHud = newValue)
                .build());

        hudCategory.addEntry(entryBuilder
                .startBooleanToggle(
                        new TranslatableText("option.advancedautopilot.showConfigOptionsInHud"),
                        config.showConfigOptionsInHud)
                .setDefaultValue(Config.DEFAULT_SHOW_CONFIG_OPTIONS_IN_HUD)
                .setSaveConsumer(newValue -> config.showConfigOptionsInHud = newValue)
                .build());

        hudCategory.addEntry(entryBuilder
                .startDoubleField(new TranslatableText("option.advancedautopilot.hudTextWidth"), config.hudTextWidth)
                .setDefaultValue(Config.DEFAULT_HUD_TEXT_WIDTH)
                .setSaveConsumer(newValue -> config.hudTextWidth = newValue)
                .build());

        hudCategory.addEntry(entryBuilder
                .startDoubleField(new TranslatableText("option.advancedautopilot.hudTextHeight"), config.hudTextHeight)
                .setDefaultValue(Config.DEFAULT_HUD_TEXT_HEIGHT)
                .setSaveConsumer(newValue -> config.hudTextHeight = newValue)
                .build());
    }

    private static void addAscendingCategory(
            ConfigBuilder builder,
            ConfigEntryBuilder entryBuilder,
            Config config) {

        ConfigCategory ascendingCategory = builder
                .getOrCreateCategory(new TranslatableText("category.advancedautopilot.ascending"));

        ascendingCategory.addEntry(entryBuilder
                .startDoubleField(
                        new TranslatableText("option.advancedautopilot.ascentHeight"),
                        config.ascentHeight)
                .setDefaultValue(Config.DEFAULT_ASCENT_HEIGHT)
                .setMin(MIN_HEIGHT)
                .setSaveConsumer(newValue -> config.ascentHeight = newValue)
                .build());

        ascendingCategory.addEntry(entryBuilder
                .startDoubleField(
                        new TranslatableText("option.advancedautopilot.maxAscendingVerticalVelocity"),
                        config.maxAscendingVerticalVelocity)
                .setDefaultValue(Config.DEFAULT_MAX_ASCENDING_VERTICAL_VELOCITY)
                .setSaveConsumer(newValue -> config.maxAscendingVerticalVelocity = newValue)
                .build());
    }

    private static void addGlidingCategory(
            ConfigBuilder builder,
            ConfigEntryBuilder entryBuilder,
            Config config) {

        ConfigCategory glidingCategory = builder
                .getOrCreateCategory(new TranslatableText("category.advancedautopilot.gliding"));

        glidingCategory.addEntry(entryBuilder
                .startDoubleField(
                        new TranslatableText("option.advancedautopilot.minHeightWhileGliding"),
                        config.minHeightWhileGliding)
                .setDefaultValue(Config.DEFAULT_MIN_HEIGHT_WHILE_GLIDING)
                .setMin(MIN_HEIGHT)
                .setSaveConsumer(newValue -> config.minHeightWhileGliding = newValue)
                .build());

        glidingCategory.addEntry(entryBuilder
                .startDoubleField(
                        new TranslatableText("option.advancedautopilot.minHeightBeforePullingUp"),
                        config.minHeightBeforePullingUp)
                .setDefaultValue(Config.DEFAULT_MIN_HEIGHT_BEFORE_PULLING_UP)
                .setMin(MIN_HEIGHT)
                .setSaveConsumer(newValue -> config.minHeightBeforePullingUp = newValue)
                .build());

        glidingCategory.addEntry(entryBuilder
                .startDoubleField(
                        new TranslatableText("option.advancedautopilot.maxHeightBeforePullingDown"),
                        config.maxHeightBeforePullingDown)
                .setDefaultValue(Config.DEFAULT_MAX_HEIGHT_BEFORE_PULLING_DOWN)
                .setMin(MIN_HEIGHT)
                .setSaveConsumer(newValue -> config.maxHeightBeforePullingDown = newValue)
                .build());
    }

    private static void addLandingCategory(
            ConfigBuilder builder,
            ConfigEntryBuilder entryBuilder,
            Config config) {

        ConfigCategory landingCategory = builder
                .getOrCreateCategory(new TranslatableText("category.advancedautopilot.landing"));

        landingCategory.addEntry(entryBuilder
                .startDoubleField(
                        new TranslatableText("option.advancedautopilot.landingPitch"),
                        config.landingPitch)
                .setDefaultValue(Config.DEFAULT_LANDING_PITCH)
                .setMin(MIN_WRAPPED_ANGLE)
                .setMax(MAX_WRAPPED_ANGLE)
                .setSaveConsumer(newValue -> config.landingPitch = newValue)
                .build());

        landingCategory.addEntry(entryBuilder
                .startDoubleField(
                        new TranslatableText("option.advancedautopilot.maxLandingSpeed"),
                        config.maxLandingSpeed)
                .setDefaultValue(Config.DEFAULT_MAX_LANDING_SPEED)
                .setMin(MIN_SPEED)
                .setSaveConsumer(newValue -> config.maxLandingSpeed = newValue)
                .build());

        landingCategory.addEntry(entryBuilder
                .startBooleanToggle(new TranslatableText("option.advancedautopilot.riskyLanding"), config.riskyLanding)
                .setDefaultValue(Config.DEFAULT_RISKY_LANDING)
                .setSaveConsumer(newValue -> config.riskyLanding = newValue)
                .build());

        landingCategory.addEntry(entryBuilder
                .startDoubleField(
                        new TranslatableText("option.advancedautopilot.minRiskyLandingHeight"),
                        config.minRiskyLandingHeight)
                .setDefaultValue(Config.DEFAULT_MIN_RISKY_LANDING_HEIGHT)
                .setMin(MIN_HEIGHT)
                .setSaveConsumer(newValue -> config.minRiskyLandingHeight = newValue)
                .build());
    }

    private static File findConfigFile() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        File configFile = Paths.get(configDir.toString(), "advancedautopilot", "config.json").toFile();
        configFile.getParentFile().mkdirs();
        try {
            configFile.createNewFile();
        } catch (IOException e) {
            AdvancedAutopilotMod.LOGGER.error("Failed to create config file", e);
        }
        return configFile;
    }

    private static Config loadConfigFromFile(File configFile) {
        Gson gson = new Gson();
        try {
            Reader reader = Files.newBufferedReader(configFile.toPath());
            Config config = gson.fromJson(reader, Config.class);
            if (config == null) {
                AdvancedAutopilotMod.LOGGER.warn("Config is null; falling back to default config");
                return new Config();
            } else {
                return config;
            }
        } catch (IOException e) {
            AdvancedAutopilotMod.LOGGER.error("Failed to load config from file; falling back to default config", e);
            return new Config();
        }
    }

    private static void saveConfigToFile(Config config, File configFile) {
        Gson gson = new Gson();
        String configString = gson.toJson(config);
        try {
            FileWriter writer = new FileWriter(configFile);
            writer.write(configString);
            writer.close();
            AdvancedAutopilotMod.LOGGER.info("Saved config to file");
        } catch (IOException e) {
            AdvancedAutopilotMod.LOGGER.error("Failed to save config to file", e);
        }
    }

}
