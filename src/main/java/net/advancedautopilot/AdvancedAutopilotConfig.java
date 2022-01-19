package net.advancedautopilot;

public class AdvancedAutopilotConfig {

    public float guiX = 5f;
    public float guiY = 5f;

    public double ascentHeight = 240d;

    public double maxLandingSpeed = 5d;

    public double minHeightToStartGliding = 180d;
    public double maxHeightWhileGliding = 360d;

    // These are magic witchcraft, don't touch them
    public double pullUpPitch = -46.633514;
    public double pullDownPitch = 37.19872;

    // TODO: optimize these further
    public double minSpeedBeforePullingDown = 10d;
    public double maxSpeedBeforePullingUp = 40d;
}
