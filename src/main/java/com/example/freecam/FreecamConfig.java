package com.example.freecam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Configuration persistante, sauvegardée dans config/freecam.json. */
public final class FreecamConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static FreecamConfig instance;

    /** Blocs par tick (20 ticks = 1 seconde). 0.6 ≈ 12 blocs/s. */
    public float speed = 0.6f;
    public float sprintMultiplier = 3.0f;
    public float verticalMultiplier = 1.0f;
    /** 0 = réaction instantanée, proche de 1 = très glissant. */
    public float smoothing = 0.5f;
    public boolean followLook = true;
    public boolean allowInteraction = false;
    public boolean disableOnDamage = true;
    public boolean showMessages = true;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("freecam.json");
    }

    public static FreecamConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                instance = GSON.fromJson(Files.readString(p), FreecamConfig.class);
            }
        } catch (Exception e) {
            System.err.println("[Freecam] Impossible de lire la config : " + e);
        }
        if (instance == null) instance = new FreecamConfig();
        instance.clamp();
    }

    public static void save() {
        try {
            instance.clamp();
            Files.writeString(path(), GSON.toJson(instance));
        } catch (IOException e) {
            System.err.println("[Freecam] Impossible d'écrire la config : " + e);
        }
    }

    public void resetToDefaults() {
        FreecamConfig d = new FreecamConfig();
        speed = d.speed;
        sprintMultiplier = d.sprintMultiplier;
        verticalMultiplier = d.verticalMultiplier;
        smoothing = d.smoothing;
        followLook = d.followLook;
        allowInteraction = d.allowInteraction;
        disableOnDamage = d.disableOnDamage;
        showMessages = d.showMessages;
    }

    public void clamp() {
        speed = Mth.clamp(speed, 0.05f, 5.0f);
        sprintMultiplier = Mth.clamp(sprintMultiplier, 1.0f, 10.0f);
        verticalMultiplier = Mth.clamp(verticalMultiplier, 0.25f, 3.0f);
        smoothing = Mth.clamp(smoothing, 0.0f, 0.95f);
    }
}
