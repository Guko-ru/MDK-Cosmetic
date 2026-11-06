package com.mdk.cosmetic.client;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.RenderType;
/** Applies the native entity-specific pose at draw time while retaining the custom geometry and visibility. */
public final class NativeArmorPoseModel extends Model<HumanoidRenderState> {
 private final HumanoidModel selected,context;
 public NativeArmorPoseModel(HumanoidModel<?> selected,HumanoidModel<?> context){super(selected.root(),RenderType::armorCutoutNoCull);this.selected=selected;this.context=context;}
 @Override @SuppressWarnings("unchecked") public void setupAnim(HumanoidRenderState state){
  selected.setupAnim(state);context.setupAnim(state);
  selected.head.loadPose(context.head.storePose());selected.hat.loadPose(context.hat.storePose());
  selected.body.loadPose(context.body.storePose());selected.leftArm.loadPose(context.leftArm.storePose());selected.rightArm.loadPose(context.rightArm.storePose());
  selected.leftLeg.loadPose(context.leftLeg.storePose());selected.rightLeg.loadPose(context.rightLeg.storePose());
 }
}
