package com.mdk.cosmetic.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

/** Small original bonsai-like sakura tree worn as a head cosmetic. */
public final class SakuraThinkerModel extends HumanoidModel<net.minecraft.client.renderer.entity.state.HumanoidRenderState> {
    public SakuraThinkerModel() {
        super(createLayer().bakeRoot());
        com.mdk.cosmetic.client.ModelVisibility.set(this,false);
        head.visible = true;
    }

    private static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0F);
        PartDefinition root = mesh.getRoot();
        root.getChild("head").addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("pot", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, -10.0F, -3.0F, 6.0F, 2.0F, 6.0F)
                .texOffs(24, 0).addBox(-2.0F, -8.25F, -2.0F, 4.0F, 1.0F, 4.0F), PartPose.ZERO);
        head.addOrReplaceChild("trunk", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 4.0F, 1.0F)
                .texOffs(6, 16).addBox(0.0F, -15.0F, -0.4F, 1.0F, 2.0F, 1.0F), PartPose.ZERO);
        head.addOrReplaceChild("leaves_main", CubeListBuilder.create()
                .texOffs(0, 24).addBox(-3.5F, -17.0F, -3.5F, 7.0F, 3.0F, 7.0F)
                .texOffs(28, 24).addBox(-2.5F, -19.0F, -2.5F, 5.0F, 2.0F, 5.0F), PartPose.ZERO);
        head.addOrReplaceChild("leaf_side_1", CubeListBuilder.create()
                .texOffs(20, 8).addBox(-2.0F, -15.5F, 2.5F, 4.0F, 2.0F, 2.0F), PartPose.ZERO);
        head.addOrReplaceChild("leaf_side_2", CubeListBuilder.create()
                .texOffs(20, 8).addBox(-2.0F, -16.0F, -4.5F, 4.0F, 2.0F, 2.0F), PartPose.ZERO);
        head.addOrReplaceChild("leaf_side_3", CubeListBuilder.create()
                .texOffs(20, 13).addBox(2.5F, -16.0F, -2.0F, 2.0F, 2.0F, 4.0F), PartPose.ZERO);
        head.addOrReplaceChild("leaf_side_4", CubeListBuilder.create()
                .texOffs(20, 13).addBox(-4.5F, -15.5F, -2.0F, 2.0F, 2.0F, 4.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }
}
