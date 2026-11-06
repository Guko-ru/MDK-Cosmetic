package com.mdk.cosmetic.mixin.client;
import com.mdk.cosmetic.client.ClientEvents;
import com.mdk.cosmetic.client.UnoEncounterOverlay;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Camera.class) public abstract class CameraEffectsMixin {
 @Shadow protected abstract void setRotation(float yaw,float pitch);
 @Inject(method="setup",at=@At("TAIL")) private void mdk$camera(CallbackInfo ci){var camera=(Camera)(Object)this;var event=new ClientEvents.ViewportEvent.ComputeCameraAngles(camera.xRot(),camera.yRot(),0);UnoEncounterOverlay.onCameraAngles(event);if(event.getYaw()!=camera.yRot()||event.getPitch()!=camera.xRot())setRotation(event.getYaw(),event.getPitch());if(event.getRoll()!=0)camera.rotation().rotateZ((float)Math.toRadians(event.getRoll()));}
}
