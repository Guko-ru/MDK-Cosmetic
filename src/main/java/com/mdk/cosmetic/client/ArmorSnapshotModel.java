package com.mdk.cosmetic.client;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.RenderType;
/** Each deferred draw restores its own animated bone poses and visibility, even when the model is reused. */
public final class ArmorSnapshotModel extends Model<ArmorSnapshotModel.Snapshot> {
 public record Snapshot(PartPose[] poses,boolean[] visible,boolean[] skipDraw) {}
 public ArmorSnapshotModel(ModelPart root){super(root,RenderType::armorCutoutNoCull);}
 public Snapshot capture(){var parts=allParts();var poses=new PartPose[parts.size()];var visible=new boolean[parts.size()];var skip=new boolean[parts.size()];for(int i=0;i<parts.size();i++){var part=parts.get(i);poses[i]=part.storePose();visible[i]=part.visible;skip[i]=part.skipDraw;}return new Snapshot(poses,visible,skip);}
 @Override public void setupAnim(Snapshot state){var parts=allParts();for(int i=0;i<parts.size();i++){var part=parts.get(i);part.loadPose(state.poses()[i]);part.visible=state.visible()[i];part.skipDraw=state.skipDraw()[i];}}
}
