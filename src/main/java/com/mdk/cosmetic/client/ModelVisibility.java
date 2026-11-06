package com.mdk.cosmetic.client;
import net.minecraft.client.model.HumanoidModel;
/** Matches the seven native humanoid part flags without hiding the parent root. */
public final class ModelVisibility {
 public static void set(HumanoidModel<?> model,boolean visible){model.head.visible=visible;model.hat.visible=visible;model.body.visible=visible;model.leftArm.visible=visible;model.rightArm.visible=visible;model.leftLeg.visible=visible;model.rightLeg.visible=visible;}
}
