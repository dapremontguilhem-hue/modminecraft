package com.example.freecam.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.DoubleConsumer;

final class ConfigSlider extends AbstractSliderButton {
    private final Component label;
    private final double min;
    private final double max;
    private final DoubleConsumer setter;

    ConfigSlider(int x, int y, int width, int height, Component label,
                 double min, double max, double current, DoubleConsumer setter) {
        super(x, y, width, height, Component.empty(), (current - min) / (max - min));
        this.label = label;
        this.min = min;
        this.max = max;
        this.setter = setter;
        updateMessage();
    }

    private double real() {
        return min + value * (max - min);
    }

    @Override
    protected void updateMessage() {
        if (label == null) return; // appelé par le constructeur parent avant l'init des champs
        setMessage(Component.translatable("freecam.config.value", label,
                String.format(Locale.ROOT, "%.2f", real())));
    }

    @Override
    protected void applyValue() {
        setter.accept(real());
    }
}
