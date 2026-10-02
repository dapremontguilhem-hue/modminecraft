package com.example.freecam;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Principe : une entité invisible (jamais ajoutée au monde) sert de caméra via
 * Minecraft#setCameraEntity. L'input du vrai joueur est remplacé par un input vide
 * pour qu'il reste immobile, et la souris est redirigée vers la caméra (EntityMixin).
 */
public final class FreecamManager {
    private static ArmorStand camera;
    private static LocalPlayer owner;
    private static ClientInput savedInput;
    private static Vec3 pos = Vec3.ZERO;
    private static Vec3 velocity = Vec3.ZERO;
    private static float lastHealth;

    private FreecamManager() {}

    public static boolean isActive() {
        return camera != null;
    }

    public static void toggle(Minecraft mc) {
        if (isActive()) disable(mc, true, null);
        else enable(mc);
    }

    public static void enable(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || isActive()) return;

        ArmorStand cam = new ArmorStand(EntityType.ARMOR_STAND, mc.level);
        cam.noPhysics = true;
        cam.setNoGravity(true);
        cam.setInvisible(true);
        cam.setYRot(player.getYRot());
        cam.setXRot(player.getXRot());

        camera = cam;
        owner = player;
        pos = player.getEyePosition();
        velocity = Vec3.ZERO;
        lastHealth = player.getHealth();
        place();
        cam.setOldPosAndRot();

        savedInput = player.input;
        player.input = new ClientInput(); // input vide : le joueur ne bouge plus

        mc.setCameraEntity(cam);
        message(mc, "freecam.message.on");
    }

    public static void disable(Minecraft mc, boolean showMessage, String customKey) {
        if (!isActive()) return;
        if (mc.player != null && mc.player == owner) {
            if (savedInput != null) mc.player.input = savedInput;
            if (mc.getCameraEntity() == camera) mc.setCameraEntity(mc.player);
        }
        reset();
        if (showMessage) message(mc, customKey != null ? customKey : "freecam.message.off");
    }

    /** Nettoie l'état sans toucher au jeu (déconnexion, changement de monde...). */
    public static void reset() {
        camera = null;
        owner = null;
        savedInput = null;
        velocity = Vec3.ZERO;
    }

    public static void tick(Minecraft mc) {
        if (!isActive()) return;
        FreecamConfig cfg = FreecamConfig.get();

        // Le monde ou le joueur a changé (respawn, dimension...) : on abandonne proprement.
        if (mc.player != owner || mc.level == null || camera.level() != mc.level) {
            reset();
            return;
        }
        // Un autre système a pris la main sur la caméra.
        if (mc.getCameraEntity() != camera) {
            if (savedInput != null) owner.input = savedInput;
            reset();
            return;
        }
        if (!owner.isAlive() || (cfg.disableOnDamage && owner.getHealth() < lastHealth)) {
            disable(mc, cfg.showMessages, "freecam.message.damage");
            return;
        }
        lastHealth = owner.getHealth();

        double fwd = 0, side = 0, up = 0;
        boolean sprint = false;
        if (mc.screen == null) {
            Options o = mc.options;
            if (o.keyUp.isDown()) fwd += 1;
            if (o.keyDown.isDown()) fwd -= 1;
            if (o.keyRight.isDown()) side += 1;
            if (o.keyLeft.isDown()) side -= 1;
            if (o.keyJump.isDown()) up += 1;
            if (o.keyShift.isDown()) up -= 1;
            sprint = o.keySprint.isDown();
        }

        double yaw = Math.toRadians(camera.getYRot());
        double pitch = Math.toRadians(camera.getXRot());
        double cp = cfg.followLook ? Math.cos(pitch) : 1.0;
        Vec3 forward = new Vec3(-Math.sin(yaw) * cp, cfg.followLook ? -Math.sin(pitch) : 0.0, Math.cos(yaw) * cp);
        Vec3 right = new Vec3(-Math.cos(yaw), 0.0, -Math.sin(yaw));

        Vec3 dir = forward.scale(fwd).add(right.scale(side)).add(0.0, up, 0.0);
        if (dir.lengthSqr() > 1.0) dir = dir.normalize();

        double speed = cfg.speed * (sprint ? cfg.sprintMultiplier : 1.0f);
        Vec3 target = new Vec3(dir.x * speed, dir.y * speed * cfg.verticalMultiplier, dir.z * speed);

        velocity = velocity.lerp(target, 1.0 - cfg.smoothing);
        if (velocity.lengthSqr() < 1.0e-8) velocity = Vec3.ZERO;

        camera.setOldPosAndRot();
        pos = pos.add(velocity);
        place();
    }

    /** Appelé par EntityMixin : redirige la souris vers la caméra. */
    public static boolean redirectTurn(Entity entity, double yRot, double xRot) {
        if (camera == null || entity != owner) return false;
        camera.turn(yRot, xRot);
        return true;
    }

    private static void place() {
        // La caméra se place aux yeux de l'entité : on compense la hauteur des yeux.
        camera.setPos(pos.x, pos.y - camera.getEyeHeight(), pos.z);
    }

    private static void message(Minecraft mc, String key) {
        if (mc.player != null && FreecamConfig.get().showMessages) {
            mc.player.displayClientMessage(Component.translatable(key), true);
        }
    }
}
