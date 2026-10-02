package com.example.freecam.gui;

import com.example.freecam.FreecamConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Écran de réglages. On ne surcharge volontairement pas render() : seuls des widgets
 * standards sont utilisés, ce qui limite les risques de casse entre versions.
 */
public class FreecamConfigScreen extends Screen {
    private final Screen parent;
    private final FreecamConfig cfg = FreecamConfig.get();

    public FreecamConfigScreen(Screen parent) {
        super(Component.translatable("freecam.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int colW = 150, gap = 10, h = 20, step = 24;
        int leftX = this.width / 2 - colW - gap / 2;
        int rightX = this.width / 2 + gap / 2;
        int y = Math.max(40, this.height / 2 - 70);

        addRenderableWidget(new StringWidget(0, y - 28, this.width, 9, this.title, this.font).alignCenter());

        // Colonne de gauche : curseurs
        addRenderableWidget(new ConfigSlider(leftX, y, colW, h,
                Component.translatable("freecam.config.speed"), 0.05, 5.0, cfg.speed, v -> cfg.speed = (float) v));
        addRenderableWidget(new ConfigSlider(leftX, y + step, colW, h,
                Component.translatable("freecam.config.sprint"), 1.0, 10.0, cfg.sprintMultiplier, v -> cfg.sprintMultiplier = (float) v));
        addRenderableWidget(new ConfigSlider(leftX, y + step * 2, colW, h,
                Component.translatable("freecam.config.vertical"), 0.25, 3.0, cfg.verticalMultiplier, v -> cfg.verticalMultiplier = (float) v));
        addRenderableWidget(new ConfigSlider(leftX, y + step * 3, colW, h,
                Component.translatable("freecam.config.smoothing"), 0.0, 0.95, cfg.smoothing, v -> cfg.smoothing = (float) v));

        // Colonne de droite : interrupteurs
        addRenderableWidget(CycleButton.onOffBuilder(cfg.followLook).create(rightX, y, colW, h,
                Component.translatable("freecam.config.follow_look"), (b, v) -> cfg.followLook = v));
        addRenderableWidget(CycleButton.onOffBuilder(cfg.allowInteraction).create(rightX, y + step, colW, h,
                Component.translatable("freecam.config.allow_interaction"), (b, v) -> cfg.allowInteraction = v));
        addRenderableWidget(CycleButton.onOffBuilder(cfg.disableOnDamage).create(rightX, y + step * 2, colW, h,
                Component.translatable("freecam.config.disable_on_damage"), (b, v) -> cfg.disableOnDamage = v));
        addRenderableWidget(CycleButton.onOffBuilder(cfg.showMessages).create(rightX, y + step * 3, colW, h,
                Component.translatable("freecam.config.show_messages"), (b, v) -> cfg.showMessages = v));

        // Bas : réinitialiser / terminé
        addRenderableWidget(Button.builder(Component.translatable("freecam.config.reset"), b -> {
            cfg.resetToDefaults();
            rebuildWidgets();
        }).bounds(leftX, y + step * 5, colW, h).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(rightX, y + step * 5, colW, h).build());
    }

    @Override
    public void onClose() {
        FreecamConfig.save();
        this.minecraft.setScreen(parent);
    }
}
