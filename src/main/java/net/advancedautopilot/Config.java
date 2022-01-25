package net.advancedautopilot;

public class Config {

    // Default values

    public static final boolean DEFAULT_SWAP_ELYTRA = true;
    public static final int DEFAULT_MAX_ELYTRA_SWAP_DURABILITY = 5;
    public static final int DEFAULT_MIN_ELYTRA_SWAP_REPLACEMENT_DURABILITY = 50;

    public static final boolean DEFAULT_EMERGENCY_LANDING = true;
    public static final int DEFAULT_MAX_EMERGENCY_LANDING_DURABILITY = 50;

    public static final boolean DEFAULT_POWERED_FLIGHT = false;
    public static final double DEFAULT_MAX_POWERED_FLIGHT_SPEED = 15d;

    public static final boolean DEFAULT_REFILL_ROCKETS = false;

    public static final boolean DEFAULT_ALLOW_UNLOADED_CHUNKS = false;
    public static final int DEFAULT_MAX_TIME_IN_UNLOADED_CHUNKS = 3;

    public static final boolean DEFAULT_RESUME_FLIGHT_TOWARDS_GOAL = false;

    public static final boolean DEFAULT_SHOW_ANGLES_IN_HUD = false;
    public static final boolean DEFAULT_SHOW_AVERAGE_SPEED_IN_HUD = false;
    public static final boolean DEFAULT_SHOW_CONFIG_OPTIONS_IN_HUD = false;
    public static final boolean DEFAULT_SHOW_PILOT_STATE_IN_HUD = false;
    public static final double DEFAULT_HUD_TEXT_WIDTH = 5f;
    public static final double DEFAULT_HUD_TEXT_HEIGHT = 5f;

    public static final double DEFAULT_ASCENT_HEIGHT = 240d;
    public static final double DEFAULT_MAX_ASCENDING_VERTICAL_VELOCITY = 15d;

    public static final double DEFAULT_MIN_HEIGHT_WHILE_GLIDING = 120d;
    public static final double DEFAULT_MIN_HEIGHT_BEFORE_PULLING_UP = 180d;
    public static final double DEFAULT_MAX_HEIGHT_BEFORE_PULLING_DOWN = 360d;

    public static final double DEFAULT_LANDING_PITCH = 30d;
    public static final double DEFAULT_MAX_LANDING_SPEED = 5d;
    public static final boolean DEFAULT_RISKY_LANDING = false;
    public static final double DEFAULT_MIN_RISKY_LANDING_HEIGHT = 150d;

    // General

    public boolean swapElytra = DEFAULT_SWAP_ELYTRA;
    public int maxElytraSwapDurability = DEFAULT_MAX_ELYTRA_SWAP_DURABILITY;
    public int minElytraSwapReplacementDurability = DEFAULT_MIN_ELYTRA_SWAP_REPLACEMENT_DURABILITY;

    public boolean emergencyLanding = DEFAULT_EMERGENCY_LANDING;
    public int maxEmergencyLandingDurability = DEFAULT_MAX_EMERGENCY_LANDING_DURABILITY;

    public boolean poweredFlight = DEFAULT_POWERED_FLIGHT;
    public double maxPoweredFlightSpeed = DEFAULT_MAX_POWERED_FLIGHT_SPEED;

    public boolean refillRockets = DEFAULT_REFILL_ROCKETS;

    public boolean allowUnloadedChunks = DEFAULT_ALLOW_UNLOADED_CHUNKS;
    public int maxTimeInUnloadedChunks = DEFAULT_MAX_TIME_IN_UNLOADED_CHUNKS;

    public boolean resumeFlightTowardsGoal = DEFAULT_RESUME_FLIGHT_TOWARDS_GOAL;

    // HUD

    public boolean showAnglesInHud = DEFAULT_SHOW_ANGLES_IN_HUD;
    public boolean showAverageSpeedInHud = DEFAULT_SHOW_AVERAGE_SPEED_IN_HUD;
    public boolean showConfigOptionsInHud = DEFAULT_SHOW_CONFIG_OPTIONS_IN_HUD;
    public boolean showPilotStateInHud = DEFAULT_SHOW_PILOT_STATE_IN_HUD;
    public double hudTextWidth = DEFAULT_HUD_TEXT_WIDTH;
    public double hudTextHeight = DEFAULT_HUD_TEXT_HEIGHT;

    // Ascending

    public double ascentHeight = DEFAULT_ASCENT_HEIGHT;
    public double maxAscendingVerticalVelocity = DEFAULT_MAX_ASCENDING_VERTICAL_VELOCITY;

    // Gliding

    public double minHeightWhileGliding = DEFAULT_MIN_HEIGHT_WHILE_GLIDING;
    public double minHeightBeforePullingUp = DEFAULT_MIN_HEIGHT_BEFORE_PULLING_UP;
    public double maxHeightBeforePullingDown = DEFAULT_MAX_HEIGHT_BEFORE_PULLING_DOWN;

    // Landing

    public double landingPitch = DEFAULT_LANDING_PITCH;
    public double maxLandingSpeed = DEFAULT_MAX_LANDING_SPEED;
    public boolean riskyLanding = DEFAULT_RISKY_LANDING;
    public double minRiskyLandingHeight = DEFAULT_MIN_RISKY_LANDING_HEIGHT;

    // Not exposed in configuration menu

    // These are magic witchcraft, don't touch them
    // https://github.com/simonlourson/fabric-elytra-auto-flight/blob/bb4e97fe475bce4231d09231af899593c52eb969/src/main/java/net/elytraautoflight/ElytraConfig.java
    public double pullUpPitch = -46.633514;
    public double pullDownPitch = 37.19872;
    public double pullUpAngularSpeed = 2.1605124 * 3;
    public double pullDownAngularSpeed = 0.20545267 * 3;
    public double minSpeedBeforePullingDown = 1.9102669 * 20d;
    public double maxSpeedBeforePullingUp = 2.3250866 * 20d;
}
