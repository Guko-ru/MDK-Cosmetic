package com.mdk.cosmetic.client;

import com.mdk.cosmetic.UnoEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Vanilla wide/slim 64x64 player mesh plus Uno's existing combat keyframes.
 * No custom clothing, weapon or other geometry is part of the body model.
 */
public final class UnoModel extends PlayerModel<UnoEntity> {
    private static final float RAD = (float) (Math.PI / 180.0);

    public UnoModel(ModelPart root, boolean slim) {
        super(root, slim);
    }

    @Override public void setupAnim(UnoEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        // Vanilla player model supplies 64x64 skin UVs (including hat/jacket/sleeves).
        this.rightArmPose = HumanoidModel.ArmPose.ITEM;
        this.leftArmPose = HumanoidModel.ArmPose.ITEM;
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float walk = Mth.clamp(limbSwingAmount,0F,1F);
        float cycle = Mth.cos(limbSwing*0.6662F);
        rightLeg.xRot = cycle*1.1F*walk;
        leftLeg.xRot = -cycle*1.1F*walk;
        rightArm.xRot = -cycle*0.65F*walk;
        leftArm.xRot = cycle*0.65F*walk;
        body.xRot = Mth.sin(ageInTicks*0.08F)*0.024F;
        head.yRot = netHeadYaw * RAD;
        head.xRot = headPitch * RAD;
        int action=entity.getAction();
        if (action == UnoEntity.IDLE) return;
        float t = entity.actionSeconds();
        // Action keyframes intentionally override the walking pose, then return to idle.
        rightArm.setRotation(0F,0F,0F); leftArm.setRotation(0F,0F,0F);
        rightLeg.setRotation(0F,0F,0F); leftLeg.setRotation(0F,0F,0F);
        body.setRotation(0F,0F,0F);
        switch (action) {
            case UnoEntity.SLASH_LEFT -> applySlashLeft(t);
            case UnoEntity.SLASH_RIGHT -> applySlashRight(t);
            case UnoEntity.HEAVY_SLASH -> applyHeavySlash(t);
            case UnoEntity.UPPERCUT -> applyUppercut(t);
            case UnoEntity.DODGE -> applyDodge(t);
            case UnoEntity.DASH -> applyDash(t);
            case UnoEntity.SWEEP_BURST -> applySweepBurst(t);
            case UnoEntity.FINISHER -> applyFinisher(t);
            case UnoEntity.EAT_APPLE -> {
                rightArm.xRot = -1.5F + Mth.sin(ageInTicks * 2.7F)*0.18F;
                rightArm.yRot = -0.17F;
                leftArm.xRot = -1.3F + Mth.sin(ageInTicks * 2.7F)*0.15F;
                leftArm.yRot = 0.15F;
            }
            default -> { }
        }
        // Keep the outer skin layer attached to the reposed limbs during sword combos.
        leftSleeve.copyFrom(leftArm);
        rightSleeve.copyFrom(rightArm);
        leftPants.copyFrom(leftLeg);
        rightPants.copyFrom(rightLeg);
        jacket.copyFrom(body);
    }
    private static float[] sample(float time, float[][] keys) {
        if (time <= keys[0][0]) return new float[]{keys[0][1], keys[0][2], keys[0][3]};
        for (int i=1; i<keys.length; i++) {
            float[] b=keys[i],a=keys[i-1];
            if (time <= b[0]) {
                float alpha=Mth.clamp((time-a[0]) / Math.max(0.0001F, b[0]-a[0]),0F,1F);
                alpha=alpha*alpha*(3F-2F*alpha); // smoothstep between keyframes
                return new float[]{Mth.lerp(alpha,a[1],b[1]),Mth.lerp(alpha,a[2],b[2]),Mth.lerp(alpha,a[3],b[3])};
            }
        }
        float[] last=keys[keys.length-1];
        return new float[]{last[1],last[2],last[3]};
    }
    private static void rotate(ModelPart p,float[] degrees) {
        p.setRotation(degrees[0]*RAD,degrees[1]*RAD,degrees[2]*RAD);
    }
    private void applySlashLeft(float t) {
        rotate(body,sample(t,new float[][]{{0.000F,0.000F,-12.000F,0.000F},{0.170F,0.000F,-38.000F,0.000F},{0.290F,0.000F,33.000F,0.000F},{0.480F,4.000F,22.000F,0.000F},{0.900F,0.000F,0.000F,0.000F}}));
        rotate(rightArm,sample(t,new float[][]{{0.000F,-15.000F,-12.000F,20.000F},{0.170F,-68.000F,-60.000F,34.000F},{0.290F,-35.000F,22.000F,-60.000F},{0.480F,52.000F,43.000F,-15.000F},{0.900F,0.000F,0.000F,0.000F}}));
        rotate(leftArm,sample(t,new float[][]{{0.000F,10.000F,0.000F,-6.000F},{0.170F,25.000F,-5.000F,-15.000F},{0.290F,-18.000F,0.000F,25.000F},{0.480F,11.000F,5.000F,12.000F},{0.900F,0.000F,0.000F,0.000F}}));
        rotate(rightLeg,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.170F,0.000F,0.000F,0.000F},{0.290F,0.000F,0.000F,0.000F},{0.480F,0.000F,0.000F,0.000F},{0.900F,0.000F,0.000F,0.000F}}));
        rotate(leftLeg,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.170F,0.000F,0.000F,0.000F},{0.290F,0.000F,0.000F,0.000F},{0.480F,0.000F,0.000F,0.000F},{0.900F,0.000F,0.000F,0.000F}}));
    }
    private void applySlashRight(float t) {
        rotate(body,sample(t,new float[][]{{0.000F,0.000F,28.000F,0.000F},{0.150F,0.000F,36.000F,0.000F},{0.330F,0.000F,-28.000F,0.000F},{0.560F,0.000F,-10.000F,0.000F},{0.900F,0.000F,0.000F,0.000F}}));
        rotate(rightArm,sample(t,new float[][]{{0.000F,27.000F,40.000F,-18.000F},{0.150F,-72.000F,62.000F,0.000F},{0.330F,-58.000F,-45.000F,18.000F},{0.560F,25.000F,-10.000F,0.000F},{0.900F,0.000F,0.000F,0.000F}}));
        rotate(leftArm,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.150F,-22.000F,0.000F,7.000F},{0.330F,12.000F,0.000F,-18.000F},{0.560F,0.000F,0.000F,12.000F},{0.900F,0.000F,0.000F,0.000F}}));
        rotate(rightLeg,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.150F,0.000F,0.000F,0.000F},{0.330F,0.000F,0.000F,0.000F},{0.560F,0.000F,0.000F,0.000F},{0.900F,0.000F,0.000F,0.000F}}));
        rotate(leftLeg,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.150F,0.000F,0.000F,0.000F},{0.330F,0.000F,0.000F,0.000F},{0.560F,0.000F,0.000F,0.000F},{0.900F,0.000F,0.000F,0.000F}}));
    }
    private void applyHeavySlash(float t) {
        rotate(body,sample(t,new float[][]{{0.000F,-8.000F,-10.000F,0.000F},{0.250F,-12.000F,-16.000F,0.000F},{0.430F,-20.000F,0.000F,0.000F},{0.620F,25.000F,8.000F,0.000F},{1.250F,0.000F,0.000F,0.000F}}));
        rotate(rightArm,sample(t,new float[][]{{0.000F,-40.000F,0.000F,12.000F},{0.250F,-145.000F,-5.000F,5.000F},{0.430F,-160.000F,0.000F,0.000F},{0.620F,48.000F,0.000F,-10.000F},{1.250F,0.000F,0.000F,0.000F}}));
        rotate(leftArm,sample(t,new float[][]{{0.000F,-25.000F,0.000F,-16.000F},{0.250F,-115.000F,-8.000F,-17.000F},{0.430F,-130.000F,0.000F,-10.000F},{0.620F,40.000F,0.000F,15.000F},{1.250F,0.000F,0.000F,0.000F}}));
        rotate(rightLeg,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.250F,0.000F,0.000F,0.000F},{0.430F,0.000F,0.000F,0.000F},{0.620F,0.000F,0.000F,0.000F},{1.250F,0.000F,0.000F,0.000F}}));
        rotate(leftLeg,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.250F,0.000F,0.000F,0.000F},{0.430F,0.000F,0.000F,0.000F},{0.620F,0.000F,0.000F,0.000F},{1.250F,0.000F,0.000F,0.000F}}));
    }
    private void applyUppercut(float t) {
        rotate(body,sample(t,new float[][]{{0.000F,18.000F,0.000F,0.000F},{0.190F,27.000F,0.000F,0.000F},{0.320F,-18.000F,8.000F,0.000F},{0.500F,-30.000F,5.000F,0.000F},{1.100F,0.000F,0.000F,0.000F}}));
        rotate(rightArm,sample(t,new float[][]{{0.000F,38.000F,-10.000F,0.000F},{0.190F,62.000F,-10.000F,10.000F},{0.320F,-130.000F,15.000F,-6.000F},{0.500F,-160.000F,14.000F,-12.000F},{1.100F,0.000F,0.000F,0.000F}}));
        rotate(leftArm,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.190F,0.000F,0.000F,0.000F},{0.320F,28.000F,0.000F,3.000F},{0.500F,0.000F,0.000F,0.000F},{1.100F,0.000F,0.000F,0.000F}}));
        rotate(rightLeg,sample(t,new float[][]{{0.000F,-20.000F,0.000F,0.000F},{0.190F,-38.000F,0.000F,0.000F},{0.320F,22.000F,0.000F,0.000F},{0.500F,18.000F,0.000F,0.000F},{1.100F,0.000F,0.000F,0.000F}}));
        rotate(leftLeg,sample(t,new float[][]{{0.000F,15.000F,0.000F,0.000F},{0.190F,30.000F,0.000F,0.000F},{0.320F,-22.000F,0.000F,0.000F},{0.500F,0.000F,0.000F,0.000F},{1.100F,0.000F,0.000F,0.000F}}));
    }
    private void applyDodge(float t) {
        rotate(body,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.120F,22.000F,45.000F,-18.000F},{0.300F,8.000F,75.000F,-25.000F},{0.650F,0.000F,0.000F,0.000F}}));
        rotate(rightArm,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.120F,-24.000F,0.000F,40.000F},{0.300F,35.000F,0.000F,-15.000F},{0.650F,0.000F,0.000F,0.000F}}));
        rotate(leftArm,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.120F,15.000F,0.000F,-25.000F},{0.300F,-30.000F,0.000F,22.000F},{0.650F,0.000F,0.000F,0.000F}}));
        rotate(rightLeg,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.120F,-30.000F,0.000F,0.000F},{0.300F,15.000F,0.000F,0.000F},{0.650F,0.000F,0.000F,0.000F}}));
        rotate(leftLeg,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.120F,35.000F,0.000F,0.000F},{0.300F,-15.000F,0.000F,0.000F},{0.650F,0.000F,0.000F,0.000F}}));
    }
    private void applyDash(float t) {
        rotate(body,sample(t,new float[][]{{0.000F,10.000F,0.000F,0.000F},{0.180F,42.000F,0.000F,0.000F},{0.360F,38.000F,0.000F,0.000F},{0.600F,16.000F,0.000F,0.000F},{0.850F,0.000F,0.000F,0.000F}}));
        rotate(rightArm,sample(t,new float[][]{{0.000F,15.000F,0.000F,0.000F},{0.180F,45.000F,0.000F,12.000F},{0.360F,-30.000F,0.000F,10.000F},{0.600F,-55.000F,0.000F,0.000F},{0.850F,0.000F,0.000F,0.000F}}));
        rotate(leftArm,sample(t,new float[][]{{0.000F,20.000F,0.000F,0.000F},{0.180F,60.000F,0.000F,-18.000F},{0.360F,40.000F,0.000F,-10.000F},{0.600F,-20.000F,0.000F,0.000F},{0.850F,0.000F,0.000F,0.000F}}));
        rotate(rightLeg,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.180F,-47.000F,0.000F,0.000F},{0.360F,40.000F,0.000F,0.000F},{0.600F,0.000F,0.000F,0.000F},{0.850F,0.000F,0.000F,0.000F}}));
        rotate(leftLeg,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.180F,42.000F,0.000F,0.000F},{0.360F,-48.000F,0.000F,0.000F},{0.600F,0.000F,0.000F,0.000F},{0.850F,0.000F,0.000F,0.000F}}));
    }
    private void applySweepBurst(float t) {
        rotate(body,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.230F,5.000F,-85.000F,0.000F},{0.500F,4.000F,15.000F,0.000F},{0.820F,4.000F,160.000F,0.000F},{1.200F,3.000F,270.000F,0.000F},{1.350F,0.000F,0.000F,0.000F}}));
        rotate(rightArm,sample(t,new float[][]{{0.000F,-25.000F,0.000F,0.000F},{0.230F,-95.000F,0.000F,8.000F},{0.500F,-78.000F,0.000F,4.000F},{0.820F,-55.000F,5.000F,12.000F},{1.200F,-65.000F,0.000F,10.000F},{1.350F,0.000F,0.000F,0.000F}}));
        rotate(leftArm,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.230F,10.000F,0.000F,-14.000F},{0.500F,0.000F,0.000F,0.000F},{0.820F,20.000F,0.000F,-20.000F},{1.200F,0.000F,0.000F,0.000F},{1.350F,0.000F,0.000F,0.000F}}));
        rotate(rightLeg,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.230F,0.000F,0.000F,0.000F},{0.500F,0.000F,0.000F,0.000F},{0.820F,0.000F,0.000F,0.000F},{1.200F,0.000F,0.000F,0.000F},{1.350F,0.000F,0.000F,0.000F}}));
        rotate(leftLeg,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.230F,0.000F,0.000F,0.000F},{0.500F,0.000F,0.000F,0.000F},{0.820F,0.000F,0.000F,0.000F},{1.200F,0.000F,0.000F,0.000F},{1.350F,0.000F,0.000F,0.000F}}));
    }
    private void applyFinisher(float t) {
        rotate(body,sample(t,new float[][]{{0.000F,12.000F,-25.000F,0.000F},{0.250F,-25.000F,-30.000F,0.000F},{0.440F,-20.000F,18.000F,0.000F},{0.650F,40.000F,64.000F,0.000F},{0.820F,25.000F,14.000F,0.000F},{1.500F,0.000F,0.000F,0.000F}}));
        rotate(rightArm,sample(t,new float[][]{{0.000F,-40.000F,0.000F,-15.000F},{0.250F,-160.000F,0.000F,-20.000F},{0.440F,-180.000F,0.000F,-6.000F},{0.650F,64.000F,10.000F,0.000F},{0.820F,-35.000F,10.000F,0.000F},{1.500F,0.000F,0.000F,0.000F}}));
        rotate(leftArm,sample(t,new float[][]{{0.000F,25.000F,0.000F,15.000F},{0.250F,-75.000F,0.000F,10.000F},{0.440F,-90.000F,0.000F,15.000F},{0.650F,40.000F,0.000F,-5.000F},{0.820F,-15.000F,0.000F,10.000F},{1.500F,0.000F,0.000F,0.000F}}));
        rotate(rightLeg,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.250F,0.000F,0.000F,0.000F},{0.440F,0.000F,0.000F,0.000F},{0.650F,0.000F,0.000F,0.000F},{0.820F,0.000F,0.000F,0.000F},{1.500F,0.000F,0.000F,0.000F}}));
        rotate(leftLeg,sample(t,new float[][]{{0.000F,0.000F,0.000F,0.000F},{0.250F,0.000F,0.000F,0.000F},{0.440F,0.000F,0.000F,0.000F},{0.650F,0.000F,0.000F,0.000F},{0.820F,0.000F,0.000F,0.000F},{1.500F,0.000F,0.000F,0.000F}}));
    }

}
