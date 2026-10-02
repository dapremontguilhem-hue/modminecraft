package com.example.freecam.mixin;

import com.example.freecam.FreecamManager;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    /** La souris appelle player.turn(...) : on la détourne vers la caméra libre. */
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void freecam$redirectTurn(double yRot, double xRot, CallbackInfo ci) {
        if (FreecamManager.redirectTurn((Entity) (Object) this, yRot, xRot)) {
            ci.cancel();
        }
    }
}
