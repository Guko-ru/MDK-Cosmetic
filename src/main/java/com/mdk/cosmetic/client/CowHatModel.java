package com.mdk.cosmetic.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

/** Cow hat rebuilt from scratch in v1.17.1. Geometry is in head-local coordinates. */
public final class CowHatModel extends HumanoidModel<LivingEntity> {
    public CowHatModel() { super(createLayer().bakeRoot()); setAllVisible(false); head.visible = true; }
    private static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        addPart0(head);
        addPart1(head);
        return LayerDefinition.create(mesh, 256, 256);
    }
    private static void addPart0(PartDefinition head) {
        head.addOrReplaceChild("cow_000", CubeListBuilder.create().texOffs(64, 128).addBox(4.460F, -2.982F, -2.788F, 1.115F, 1.115F, 1.115F), PartPose.ZERO);
        head.addOrReplaceChild("cow_001", CubeListBuilder.create().texOffs(128, 128).addBox(5.018F, -3.540F, -2.788F, 0.557F, 0.557F, 1.115F), PartPose.ZERO);
        head.addOrReplaceChild("cow_002", CubeListBuilder.create().texOffs(192, 128).addBox(5.018F, -4.098F, -2.788F, 0.557F, 0.557F, 1.115F), PartPose.ZERO);
        head.addOrReplaceChild("cow_003", CubeListBuilder.create().texOffs(128, 0).addBox(-5.018F, -4.655F, -5.575F, 10.035F, 0.557F, 1.115F), PartPose.ZERO);
        head.addOrReplaceChild("cow_004", CubeListBuilder.create().texOffs(128, 0).addBox(-5.575F, -4.655F, -4.460F, 1.115F, 0.557F, 8.920F), PartPose.ZERO);
        head.addOrReplaceChild("cow_005", CubeListBuilder.create().texOffs(128, 0).addBox(4.460F, -4.655F, -4.460F, 1.115F, 0.557F, 8.920F), PartPose.ZERO);
        head.addOrReplaceChild("cow_006", CubeListBuilder.create().texOffs(128, 0).addBox(-5.018F, -4.655F, 4.460F, 10.035F, 0.557F, 1.115F), PartPose.ZERO);
        head.addOrReplaceChild("cow_007", CubeListBuilder.create().texOffs(0, 0).addBox(-5.575F, -6.885F, -5.575F, 11.150F, 2.230F, 1.115F), PartPose.ZERO);
        head.addOrReplaceChild("cow_008", CubeListBuilder.create().texOffs(0, 0).addBox(-5.575F, -5.770F, -4.460F, 1.115F, 1.115F, 10.035F), PartPose.ZERO);
        head.addOrReplaceChild("cow_009", CubeListBuilder.create().texOffs(0, 0).addBox(4.460F, -6.885F, -4.460F, 1.115F, 2.230F, 10.035F), PartPose.ZERO);
        head.addOrReplaceChild("cow_010", CubeListBuilder.create().texOffs(0, 0).addBox(-4.460F, -5.213F, 4.460F, 8.920F, 0.557F, 1.115F), PartPose.ZERO);
        head.addOrReplaceChild("cow_011", CubeListBuilder.create().texOffs(64, 64).addBox(-1.115F, -6.328F, -6.690F, 2.230F, 1.115F, 0.557F), PartPose.ZERO);
        head.addOrReplaceChild("cow_012", CubeListBuilder.create().texOffs(0, 0).addBox(-4.460F, -6.885F, 4.460F, 3.345F, 1.672F, 1.115F), PartPose.ZERO);
        head.addOrReplaceChild("cow_013", CubeListBuilder.create().texOffs(192, 0).addBox(-1.115F, -6.885F, 4.460F, 2.230F, 1.672F, 1.115F), PartPose.ZERO);
        head.addOrReplaceChild("cow_014", CubeListBuilder.create().texOffs(0, 0).addBox(1.115F, -6.885F, 4.460F, 3.345F, 1.672F, 1.115F), PartPose.ZERO);
        head.addOrReplaceChild("cow_015", CubeListBuilder.create().texOffs(128, 64).addBox(-1.115F, -6.328F, -7.247F, 2.230F, 0.557F, 0.557F), PartPose.ZERO);
        head.addOrReplaceChild("cow_016", CubeListBuilder.create().texOffs(128, 64).addBox(-2.788F, -6.328F, -6.690F, 0.557F, 0.557F, 0.557F), PartPose.ZERO);
        head.addOrReplaceChild("cow_017", CubeListBuilder.create().texOffs(128, 64).addBox(2.230F, -6.328F, -6.690F, 0.557F, 0.557F, 0.557F), PartPose.ZERO);
        head.addOrReplaceChild("cow_018", CubeListBuilder.create().texOffs(64, 0).addBox(-2.788F, -8.000F, -6.133F, 5.575F, 2.230F, 0.557F), PartPose.ZERO);
        head.addOrReplaceChild("cow_019", CubeListBuilder.create().texOffs(0, 0).addBox(-5.575F, -6.885F, -4.460F, 1.115F, 1.115F, 5.018F), PartPose.ZERO);
        head.addOrReplaceChild("cow_020", CubeListBuilder.create().texOffs(192, 0).addBox(-5.575F, -6.885F, 0.557F, 1.115F, 1.115F, 2.230F), PartPose.ZERO);
        head.addOrReplaceChild("cow_021", CubeListBuilder.create().texOffs(0, 0).addBox(-5.575F, -6.885F, 2.788F, 1.115F, 1.115F, 2.788F), PartPose.ZERO);
        head.addOrReplaceChild("cow_022", CubeListBuilder.create().texOffs(0, 0).addBox(-7.247F, -9.115F, -2.788F, 1.672F, 2.788F, 3.345F), PartPose.ZERO);
        head.addOrReplaceChild("cow_023", CubeListBuilder.create().texOffs(0, 0).addBox(5.575F, -9.115F, -2.788F, 1.672F, 2.788F, 3.345F), PartPose.ZERO);
        head.addOrReplaceChild("cow_024", CubeListBuilder.create().texOffs(0, 128).addBox(-2.230F, -7.442F, -6.690F, 1.115F, 0.557F, 0.557F), PartPose.ZERO);
        head.addOrReplaceChild("cow_025", CubeListBuilder.create().texOffs(0, 128).addBox(1.115F, -7.442F, -6.690F, 1.115F, 0.557F, 0.557F), PartPose.ZERO);
        head.addOrReplaceChild("cow_026", CubeListBuilder.create().texOffs(0, 0).addBox(-5.018F, -8.000F, -5.018F, 10.035F, 1.115F, 1.672F), PartPose.ZERO);
        head.addOrReplaceChild("cow_027", CubeListBuilder.create().texOffs(64, 64).addBox(-7.247F, -8.557F, -3.345F, 1.672F, 1.672F, 0.557F), PartPose.ZERO);
        head.addOrReplaceChild("cow_028", CubeListBuilder.create().texOffs(0, 0).addBox(-5.018F, -8.000F, -3.345F, 7.247F, 1.115F, 0.557F), PartPose.ZERO);
        head.addOrReplaceChild("cow_029", CubeListBuilder.create().texOffs(192, 0).addBox(2.230F, -9.115F, -3.345F, 2.230F, 2.230F, 3.345F), PartPose.ZERO);
        head.addOrReplaceChild("cow_030", CubeListBuilder.create().texOffs(0, 0).addBox(4.460F, -8.000F, -3.345F, 0.557F, 1.115F, 0.557F), PartPose.ZERO);
        head.addOrReplaceChild("cow_031", CubeListBuilder.create().texOffs(64, 64).addBox(5.575F, -8.557F, -3.345F, 1.672F, 1.672F, 0.557F), PartPose.ZERO);
        head.addOrReplaceChild("cow_032", CubeListBuilder.create().texOffs(0, 0).addBox(-5.018F, -8.000F, -2.788F, 6.690F, 1.115F, 3.902F), PartPose.ZERO);
        head.addOrReplaceChild("cow_033", CubeListBuilder.create().texOffs(192, 0).addBox(1.672F, -10.787F, -2.788F, 0.557F, 3.902F, 2.230F), PartPose.ZERO);
        head.addOrReplaceChild("cow_034", CubeListBuilder.create().texOffs(192, 0).addBox(4.460F, -8.000F, -2.788F, 0.557F, 1.115F, 2.230F), PartPose.ZERO);
        head.addOrReplaceChild("cow_035", CubeListBuilder.create().texOffs(128, 0).addBox(-7.805F, -8.557F, -2.230F, 0.557F, 1.672F, 2.230F), PartPose.ZERO);
        head.addOrReplaceChild("cow_036", CubeListBuilder.create().texOffs(128, 0).addBox(7.247F, -8.557F, -2.230F, 0.557F, 1.672F, 2.230F), PartPose.ZERO);
        head.addOrReplaceChild("cow_037", CubeListBuilder.create().texOffs(0, 0).addBox(1.672F, -8.000F, -0.557F, 0.557F, 1.115F, 5.575F), PartPose.ZERO);
        head.addOrReplaceChild("cow_038", CubeListBuilder.create().texOffs(0, 0).addBox(4.460F, -8.000F, -0.557F, 0.557F, 1.115F, 5.575F), PartPose.ZERO);
        head.addOrReplaceChild("cow_039", CubeListBuilder.create().texOffs(0, 0).addBox(2.230F, -8.000F, 0.000F, 2.230F, 1.115F, 5.018F), PartPose.ZERO);
        head.addOrReplaceChild("cow_040", CubeListBuilder.create().texOffs(0, 0).addBox(-5.018F, -8.000F, 1.115F, 1.115F, 1.115F, 0.557F), PartPose.ZERO);
        head.addOrReplaceChild("cow_041", CubeListBuilder.create().texOffs(192, 0).addBox(-3.902F, -8.000F, 1.115F, 2.788F, 1.115F, 3.902F), PartPose.ZERO);
        head.addOrReplaceChild("cow_042", CubeListBuilder.create().texOffs(0, 0).addBox(-1.115F, -9.115F, 1.115F, 2.788F, 2.230F, 3.345F), PartPose.ZERO);
        head.addOrReplaceChild("cow_043", CubeListBuilder.create().texOffs(0, 0).addBox(-5.018F, -8.000F, 1.672F, 0.557F, 1.115F, 3.345F), PartPose.ZERO);
        head.addOrReplaceChild("cow_044", CubeListBuilder.create().texOffs(192, 0).addBox(-4.460F, -9.115F, 1.672F, 0.557F, 2.230F, 2.230F), PartPose.ZERO);
        head.addOrReplaceChild("cow_045", CubeListBuilder.create().texOffs(0, 0).addBox(-4.460F, -8.000F, 3.902F, 0.557F, 1.115F, 1.115F), PartPose.ZERO);
        head.addOrReplaceChild("cow_046", CubeListBuilder.create().texOffs(192, 0).addBox(-1.115F, -8.000F, 4.460F, 2.230F, 1.115F, 0.557F), PartPose.ZERO);
        head.addOrReplaceChild("cow_047", CubeListBuilder.create().texOffs(0, 0).addBox(1.115F, -8.000F, 4.460F, 0.557F, 1.115F, 0.557F), PartPose.ZERO);
        head.addOrReplaceChild("cow_048", CubeListBuilder.create().texOffs(0, 0).addBox(-4.460F, -9.115F, -4.460F, 8.920F, 1.115F, 1.115F), PartPose.ZERO);
        head.addOrReplaceChild("cow_049", CubeListBuilder.create().texOffs(0, 0).addBox(-4.460F, -9.115F, -3.345F, 6.690F, 1.115F, 0.557F), PartPose.ZERO);
    }
    private static void addPart1(PartDefinition head) {
        head.addOrReplaceChild("cow_050", CubeListBuilder.create().texOffs(0, 0).addBox(-4.460F, -9.115F, -2.788F, 6.133F, 1.115F, 3.902F), PartPose.ZERO);
        head.addOrReplaceChild("cow_051", CubeListBuilder.create().texOffs(0, 0).addBox(1.672F, -9.115F, -0.557F, 0.557F, 1.115F, 5.018F), PartPose.ZERO);
        head.addOrReplaceChild("cow_052", CubeListBuilder.create().texOffs(0, 0).addBox(2.230F, -9.115F, 0.000F, 2.230F, 1.115F, 4.460F), PartPose.ZERO);
        head.addOrReplaceChild("cow_053", CubeListBuilder.create().texOffs(0, 0).addBox(-4.460F, -9.115F, 1.115F, 0.557F, 1.115F, 0.557F), PartPose.ZERO);
        head.addOrReplaceChild("cow_054", CubeListBuilder.create().texOffs(192, 0).addBox(-3.902F, -9.115F, 1.115F, 2.788F, 1.115F, 3.345F), PartPose.ZERO);
        head.addOrReplaceChild("cow_055", CubeListBuilder.create().texOffs(0, 0).addBox(-4.460F, -9.115F, 3.902F, 0.557F, 1.115F, 0.557F), PartPose.ZERO);
        head.addOrReplaceChild("cow_056", CubeListBuilder.create().texOffs(64, 0).addBox(-3.902F, -10.230F, -3.902F, 7.805F, 1.115F, 0.557F), PartPose.ZERO);
        head.addOrReplaceChild("cow_057", CubeListBuilder.create().texOffs(64, 0).addBox(-3.902F, -10.230F, -3.345F, 6.133F, 1.115F, 0.557F), PartPose.ZERO);
        head.addOrReplaceChild("cow_058", CubeListBuilder.create().texOffs(192, 0).addBox(2.230F, -10.230F, -3.345F, 1.672F, 1.115F, 3.345F), PartPose.ZERO);
        head.addOrReplaceChild("cow_059", CubeListBuilder.create().texOffs(64, 0).addBox(-3.902F, -10.230F, -2.788F, 5.575F, 1.115F, 3.902F), PartPose.ZERO);
        head.addOrReplaceChild("cow_060", CubeListBuilder.create().texOffs(64, 0).addBox(1.672F, -10.230F, -0.557F, 0.557F, 1.115F, 4.460F), PartPose.ZERO);
        head.addOrReplaceChild("cow_061", CubeListBuilder.create().texOffs(64, 0).addBox(2.230F, -10.230F, 0.000F, 1.672F, 1.115F, 3.902F), PartPose.ZERO);
        head.addOrReplaceChild("cow_062", CubeListBuilder.create().texOffs(192, 0).addBox(-3.902F, -10.230F, 1.115F, 2.788F, 1.115F, 2.788F), PartPose.ZERO);
        head.addOrReplaceChild("cow_063", CubeListBuilder.create().texOffs(64, 0).addBox(-1.115F, -10.230F, 1.115F, 2.788F, 1.115F, 2.788F), PartPose.ZERO);
        head.addOrReplaceChild("cow_064", CubeListBuilder.create().texOffs(128, 0).addBox(-3.345F, -11.345F, -2.788F, 0.557F, 1.115F, 2.230F), PartPose.ZERO);
        head.addOrReplaceChild("cow_065", CubeListBuilder.create().texOffs(0, 0).addBox(-2.788F, -10.787F, -2.788F, 4.460F, 0.557F, 3.902F), PartPose.ZERO);
        head.addOrReplaceChild("cow_066", CubeListBuilder.create().texOffs(192, 0).addBox(2.230F, -10.787F, -2.788F, 0.557F, 0.557F, 2.788F), PartPose.ZERO);
        head.addOrReplaceChild("cow_067", CubeListBuilder.create().texOffs(128, 0).addBox(2.788F, -11.345F, -2.788F, 0.557F, 1.115F, 2.230F), PartPose.ZERO);
        head.addOrReplaceChild("cow_068", CubeListBuilder.create().texOffs(0, 0).addBox(1.672F, -10.787F, -0.557F, 0.557F, 0.557F, 3.345F), PartPose.ZERO);
        head.addOrReplaceChild("cow_069", CubeListBuilder.create().texOffs(0, 0).addBox(2.230F, -10.787F, 0.000F, 0.557F, 0.557F, 2.788F), PartPose.ZERO);
        head.addOrReplaceChild("cow_070", CubeListBuilder.create().texOffs(192, 0).addBox(-2.788F, -10.787F, 1.115F, 1.672F, 0.557F, 1.672F), PartPose.ZERO);
        head.addOrReplaceChild("cow_071", CubeListBuilder.create().texOffs(0, 0).addBox(-1.115F, -10.787F, 1.115F, 2.788F, 0.557F, 1.672F), PartPose.ZERO);
        head.addOrReplaceChild("cow_072", CubeListBuilder.create().texOffs(128, 0).addBox(-2.788F, -11.345F, -2.788F, 0.557F, 0.557F, 2.230F), PartPose.ZERO);
        head.addOrReplaceChild("cow_073", CubeListBuilder.create().texOffs(128, 0).addBox(2.230F, -11.345F, -2.788F, 0.557F, 0.557F, 2.230F), PartPose.ZERO);
        head.addOrReplaceChild("cow_074", CubeListBuilder.create().texOffs(64, 0).addBox(-3.345F, -12.460F, -2.230F, 1.115F, 1.115F, 1.115F), PartPose.ZERO);
        head.addOrReplaceChild("cow_075", CubeListBuilder.create().texOffs(64, 0).addBox(2.230F, -12.460F, -2.230F, 1.115F, 1.115F, 1.115F), PartPose.ZERO);
        head.addOrReplaceChild("cow_076", CubeListBuilder.create().texOffs(64, 0).addBox(-2.788F, -13.018F, -2.230F, 0.557F, 0.557F, 1.115F), PartPose.ZERO);
        head.addOrReplaceChild("cow_077", CubeListBuilder.create().texOffs(64, 0).addBox(2.230F, -13.018F, -2.230F, 0.557F, 0.557F, 1.115F), PartPose.ZERO);
    }
}
