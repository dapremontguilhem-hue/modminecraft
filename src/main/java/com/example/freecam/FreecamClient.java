package com.example.freecam;

import com.example.freecam.gui.FreecamConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;

public class FreecamClient implements ClientModInitializer {
    public static final String MOD_ID = "freecam";

    private static KeyMapping toggleKey;
    private static KeyMapping configKey;

    @Override
    public void onInitializeClient() {
        FreecamConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.freecam.toggle", GLFW.GLFW_KEY_F6, category));
        configKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.freecam.config", GLFW.GLFW_KEY_F7, category));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (toggleKey.consumeClick()) FreecamManager.toggle(mc);
            while (configKey.consumeClick()) mc.setScreen(new FreecamConfigScreen(mc.screen));
            FreecamManager.tick(mc);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FreecamManager.reset());

        // Pendant la freecam, on bloque les interactions (la caméra "viserait" depuis un endroit
        // où le joueur n'est pas, ce qui enverrait des paquets suspects au serveur).
        AttackBlockCallback.EVENT.register((player, level, hand, pos, dir) -> blocked());
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> blocked());
        UseItemCallback.EVENT.register((player, level, hand) -> blocked());
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> blocked());
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> blocked());
    }

    private static InteractionResult blocked() {
        return FreecamManager.isActive() && !FreecamConfig.get().allowInteraction
                ? InteractionResult.FAIL
                : InteractionResult.PASS;
    }
}
