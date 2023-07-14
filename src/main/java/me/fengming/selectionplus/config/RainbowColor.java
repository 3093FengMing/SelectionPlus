package me.fengming.selectionplus.config;

import me.fengming.selectionplus.SelectionPlus;

import java.awt.*;

import static java.awt.Color.HSBtoRGB;
import static java.awt.Color.RGBtoHSB;
import static me.fengming.selectionplus.Utils.*;

public class RainbowColor {
    private float hue;
    private final float saturation;
    private final float brightness;

    public RainbowColor() {
        this.hue = 0;
        this.saturation = 1;
        this.brightness = 1;
    }

    public void next() {
        hue += 0.0001 * SelectionPlus.config.setting.rainbowSpeed;
        if (hue > 1) {
            hue = 0;
        }
    }

    public Color getColor() {
        return Color.getHSBColor(hue, saturation, brightness);
    }
}
