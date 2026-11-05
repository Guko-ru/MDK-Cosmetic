package com.mdk.cosmetic.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

/** Head-only rabbit ears generated from the editable 192-cube Blockbench asset. */
/** v1.17.1 scale and positioning calibration. */
public final class RabbitEarsModel extends HumanoidModel<LivingEntity> {
    public RabbitEarsModel() {
        super(createLayer().bakeRoot());
        setAllVisible(false);
        head.visible = true;
    }
    private static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        // Fixed head-local placement: geometry moves 1 px down; the head itself keeps vanilla animations.
        PartDefinition geo = head.addOrReplaceChild("fitted_geometry", CubeListBuilder.create(), PartPose.offset(0.0F,1.0F,0.0F));
        geo.addOrReplaceChild("rabbit_000", CubeListBuilder.create().texOffs(0,0).addBox(-3.3450F, -8.5575F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_001", CubeListBuilder.create().texOffs(32,0).addBox(-3.9025F, -8.5575F, -1.6725F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_002", CubeListBuilder.create().texOffs(64,0).addBox(-3.3450F, -10.7875F, -1.6725F, 1.1150F, 2.7875F, 2.2300F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_003", CubeListBuilder.create().texOffs(96,0).addBox(-2.2300F, -9.1150F, -1.6725F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_004", CubeListBuilder.create().texOffs(128,0).addBox(-3.9025F, -8.5575F, -1.1150F, 0.5575F, 0.5575F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_005", CubeListBuilder.create().texOffs(160,0).addBox(-2.2300F, -9.1150F, -1.1150F, 0.5575F, 1.1150F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_006", CubeListBuilder.create().texOffs(192,0).addBox(-3.9025F, -8.5575F, 0.0000F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_007", CubeListBuilder.create().texOffs(224,0).addBox(-2.2300F, -9.1150F, 0.0000F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_008", CubeListBuilder.create().texOffs(256,0).addBox(-3.9025F, -9.1150F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_009", CubeListBuilder.create().texOffs(288,0).addBox(-4.4600F, -13.0175F, -1.6725F, 0.5575F, 4.4600F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_010", CubeListBuilder.create().texOffs(320,0).addBox(-3.9025F, -10.7875F, -1.6725F, 0.5575F, 2.2300F, 2.2300F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_011", CubeListBuilder.create().texOffs(352,0).addBox(-4.4600F, -13.0175F, -1.1150F, 0.5575F, 4.4600F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_012", CubeListBuilder.create().texOffs(384,0).addBox(-4.4600F, -13.0175F, 0.0000F, 0.5575F, 4.4600F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_013", CubeListBuilder.create().texOffs(416,0).addBox(-4.4600F, -9.6725F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_014", CubeListBuilder.create().texOffs(448,0).addBox(-2.7875F, -9.6725F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_015", CubeListBuilder.create().texOffs(480,0).addBox(-1.6725F, -9.6725F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_016", CubeListBuilder.create().texOffs(0,32).addBox(-2.2300F, -10.7875F, -1.6725F, 0.5575F, 1.6725F, 2.2300F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_017", CubeListBuilder.create().texOffs(32,32).addBox(-1.6725F, -9.6725F, -1.6725F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_018", CubeListBuilder.create().texOffs(64,32).addBox(-1.6725F, -9.6725F, -1.1150F, 0.5575F, 0.5575F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_019", CubeListBuilder.create().texOffs(96,32).addBox(-1.6725F, -9.6725F, 0.0000F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_020", CubeListBuilder.create().texOffs(128,32).addBox(-4.4600F, -10.7875F, -2.2300F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_021", CubeListBuilder.create().texOffs(160,32).addBox(-3.9025F, -10.7875F, -2.2300F, 2.7875F, 1.1150F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_022", CubeListBuilder.create().texOffs(192,32).addBox(-1.1150F, -10.7875F, -2.2300F, 0.5575F, 1.1150F, 1.1150F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_023", CubeListBuilder.create().texOffs(224,32).addBox(-1.6725F, -10.7875F, -1.6725F, 0.5575F, 1.1150F, 2.2300F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_024", CubeListBuilder.create().texOffs(256,32).addBox(-1.1150F, -13.0175F, -1.1150F, 0.5575F, 3.3450F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_025", CubeListBuilder.create().texOffs(288,32).addBox(-1.1150F, -13.0175F, 0.0000F, 0.5575F, 3.3450F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_026", CubeListBuilder.create().texOffs(320,32).addBox(-4.4600F, -12.4600F, -2.2300F, 0.5575F, 1.6725F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_027", CubeListBuilder.create().texOffs(352,32).addBox(-3.9025F, -13.0175F, -2.2300F, 0.5575F, 2.2300F, 0.5575F),PartPose.ZERO); // inner_rose
        geo.addOrReplaceChild("rabbit_028", CubeListBuilder.create().texOffs(384,32).addBox(-3.3450F, -11.9025F, -2.2300F, 1.6725F, 1.1150F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_029", CubeListBuilder.create().texOffs(416,32).addBox(-1.6725F, -13.0175F, -2.2300F, 0.5575F, 2.2300F, 0.5575F),PartPose.ZERO); // inner_rose
        geo.addOrReplaceChild("rabbit_030", CubeListBuilder.create().texOffs(448,32).addBox(-1.1150F, -12.4600F, -2.2300F, 0.5575F, 1.6725F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_031", CubeListBuilder.create().texOffs(480,32).addBox(-3.9025F, -11.3450F, -1.6725F, 2.7875F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_032", CubeListBuilder.create().texOffs(0,64).addBox(-1.1150F, -13.0175F, -1.6725F, 0.5575F, 2.2300F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_033", CubeListBuilder.create().texOffs(32,64).addBox(-3.9025F, -13.0175F, -1.1150F, 2.7875F, 2.2300F, 1.6725F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_034", CubeListBuilder.create().texOffs(64,64).addBox(-3.9025F, -13.0175F, -1.6725F, 2.7875F, 1.6725F, 0.5575F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_035", CubeListBuilder.create().texOffs(96,64).addBox(-3.3450F, -16.3625F, -2.2300F, 0.5575F, 4.4600F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_036", CubeListBuilder.create().texOffs(128,64).addBox(-2.7875F, -12.4600F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // inner_light
        geo.addOrReplaceChild("rabbit_037", CubeListBuilder.create().texOffs(160,64).addBox(-2.2300F, -13.0175F, -2.2300F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_038", CubeListBuilder.create().texOffs(192,64).addBox(-4.4600F, -13.0175F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_039", CubeListBuilder.create().texOffs(224,64).addBox(-2.7875F, -13.0175F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_040", CubeListBuilder.create().texOffs(256,64).addBox(-1.1150F, -13.0175F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_041", CubeListBuilder.create().texOffs(288,64).addBox(-5.5750F, -13.5750F, -2.2300F, 0.5575F, 0.5575F, 1.1150F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_042", CubeListBuilder.create().texOffs(320,64).addBox(-5.0175F, -14.6900F, -2.2300F, 0.5575F, 1.6725F, 0.5575F),PartPose.ZERO); // inner_rose
        geo.addOrReplaceChild("rabbit_043", CubeListBuilder.create().texOffs(352,64).addBox(-4.4600F, -14.1325F, -2.2300F, 1.1150F, 1.1150F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_044", CubeListBuilder.create().texOffs(384,64).addBox(-2.7875F, -15.2475F, -2.2300F, 0.5575F, 2.2300F, 0.5575F),PartPose.ZERO); // inner_rose
        geo.addOrReplaceChild("rabbit_045", CubeListBuilder.create().texOffs(416,64).addBox(-2.2300F, -13.5750F, -2.2300F, 0.5575F, 0.5575F, 1.1150F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_046", CubeListBuilder.create().texOffs(448,64).addBox(-5.0175F, -13.5750F, -1.6725F, 2.7875F, 0.5575F, 2.2300F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_047", CubeListBuilder.create().texOffs(480,64).addBox(-5.5750F, -14.6900F, -1.1150F, 0.5575F, 1.6725F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_048", CubeListBuilder.create().texOffs(0,96).addBox(-2.2300F, -16.9200F, -1.1150F, 0.5575F, 3.9025F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_049", CubeListBuilder.create().texOffs(32,96).addBox(-5.5750F, -14.6900F, 0.0000F, 0.5575F, 1.6725F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_050", CubeListBuilder.create().texOffs(64,96).addBox(-2.2300F, -16.9200F, 0.0000F, 0.5575F, 3.9025F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_051", CubeListBuilder.create().texOffs(96,96).addBox(-5.5750F, -14.6900F, -2.2300F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_052", CubeListBuilder.create().texOffs(128,96).addBox(-2.2300F, -15.2475F, -2.2300F, 0.5575F, 1.6725F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_053", CubeListBuilder.create().texOffs(160,96).addBox(-5.5750F, -14.6900F, -1.6725F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_054", CubeListBuilder.create().texOffs(192,96).addBox(-5.0175F, -14.1325F, -1.6725F, 2.7875F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_055", CubeListBuilder.create().texOffs(224,96).addBox(-2.2300F, -16.9200F, -1.6725F, 0.5575F, 3.3450F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_056", CubeListBuilder.create().texOffs(256,96).addBox(-5.0175F, -14.6900F, -1.1150F, 2.7875F, 1.1150F, 1.6725F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_057", CubeListBuilder.create().texOffs(288,96).addBox(-4.4600F, -14.6900F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_058", CubeListBuilder.create().texOffs(320,96).addBox(-3.9025F, -14.6900F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // inner_light
        geo.addOrReplaceChild("rabbit_059", CubeListBuilder.create().texOffs(352,96).addBox(-5.0175F, -14.6900F, -1.6725F, 2.7875F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_060", CubeListBuilder.create().texOffs(384,96).addBox(-5.0175F, -15.2475F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_061", CubeListBuilder.create().texOffs(416,96).addBox(-4.4600F, -15.2475F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // inner_rose
        geo.addOrReplaceChild("rabbit_062", CubeListBuilder.create().texOffs(448,96).addBox(-3.9025F, -18.0350F, -2.2300F, 0.5575F, 3.3450F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_063", CubeListBuilder.create().texOffs(480,96).addBox(-5.0175F, -16.3625F, -1.6725F, 0.5575F, 1.6725F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_064", CubeListBuilder.create().texOffs(0,128).addBox(-4.4600F, -16.3625F, -1.6725F, 2.2300F, 1.6725F, 2.2300F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_065", CubeListBuilder.create().texOffs(32,128).addBox(-5.0175F, -16.3625F, -1.1150F, 0.5575F, 1.6725F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_066", CubeListBuilder.create().texOffs(64,128).addBox(-5.0175F, -16.3625F, 0.0000F, 0.5575F, 1.6725F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_067", CubeListBuilder.create().texOffs(96,128).addBox(-5.0175F, -16.3625F, -2.2300F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_068", CubeListBuilder.create().texOffs(128,128).addBox(-4.4600F, -16.3625F, -2.2300F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_069", CubeListBuilder.create().texOffs(160,128).addBox(-2.7875F, -16.9200F, -2.2300F, 0.5575F, 1.6725F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_070", CubeListBuilder.create().texOffs(192,128).addBox(-2.2300F, -16.3625F, -2.2300F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_071", CubeListBuilder.create().texOffs(224,128).addBox(-4.4600F, -16.9200F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_072", CubeListBuilder.create().texOffs(256,128).addBox(-3.3450F, -16.9200F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // inner_light
        geo.addOrReplaceChild("rabbit_073", CubeListBuilder.create().texOffs(288,128).addBox(-2.2300F, -16.9200F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_074", CubeListBuilder.create().texOffs(320,128).addBox(-4.4600F, -16.9200F, -1.6725F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_075", CubeListBuilder.create().texOffs(352,128).addBox(-3.9025F, -16.9200F, -1.6725F, 1.6725F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_076", CubeListBuilder.create().texOffs(384,128).addBox(-4.4600F, -16.9200F, -1.1150F, 0.5575F, 0.5575F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_077", CubeListBuilder.create().texOffs(416,128).addBox(-3.9025F, -16.9200F, -1.1150F, 1.6725F, 0.5575F, 1.6725F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_078", CubeListBuilder.create().texOffs(448,128).addBox(-4.4600F, -16.9200F, 0.0000F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_079", CubeListBuilder.create().texOffs(480,128).addBox(-5.5750F, -17.4775F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_080", CubeListBuilder.create().texOffs(0,160).addBox(-5.0175F, -17.4775F, -2.2300F, 1.1150F, 0.5575F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_081", CubeListBuilder.create().texOffs(32,160).addBox(-3.3450F, -18.0350F, -2.2300F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_082", CubeListBuilder.create().texOffs(64,160).addBox(-5.5750F, -17.4775F, -1.6725F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_083", CubeListBuilder.create().texOffs(96,160).addBox(-5.0175F, -17.4775F, -1.6725F, 1.6725F, 0.5575F, 2.2300F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_084", CubeListBuilder.create().texOffs(128,160).addBox(-3.3450F, -19.7075F, -1.6725F, 0.5575F, 2.7875F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_085", CubeListBuilder.create().texOffs(160,160).addBox(-5.5750F, -17.4775F, -1.1150F, 0.5575F, 0.5575F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_086", CubeListBuilder.create().texOffs(192,160).addBox(-3.3450F, -19.7075F, -1.1150F, 0.5575F, 2.7875F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_087", CubeListBuilder.create().texOffs(224,160).addBox(-5.5750F, -17.4775F, 0.0000F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_088", CubeListBuilder.create().texOffs(256,160).addBox(-3.3450F, -19.7075F, 0.0000F, 0.5575F, 2.7875F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_089", CubeListBuilder.create().texOffs(288,160).addBox(-5.0175F, -18.0350F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_090", CubeListBuilder.create().texOffs(320,160).addBox(-4.4600F, -18.0350F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_091", CubeListBuilder.create().texOffs(352,160).addBox(-5.0175F, -18.5925F, -1.6725F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_092", CubeListBuilder.create().texOffs(384,160).addBox(-4.4600F, -18.5925F, -1.6725F, 1.1150F, 1.1150F, 2.2300F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_093", CubeListBuilder.create().texOffs(416,160).addBox(-5.0175F, -18.5925F, -1.1150F, 0.5575F, 1.1150F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_094", CubeListBuilder.create().texOffs(448,160).addBox(-5.0175F, -18.5925F, 0.0000F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_095", CubeListBuilder.create().texOffs(480,160).addBox(-5.0175F, -18.5925F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_096", CubeListBuilder.create().texOffs(0,192).addBox(-3.3450F, -19.1500F, -2.2300F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_097", CubeListBuilder.create().texOffs(32,192).addBox(-4.4600F, -19.1500F, -2.2300F, 0.5575F, 0.5575F, 1.1150F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_098", CubeListBuilder.create().texOffs(64,192).addBox(-3.9025F, -19.7075F, -1.6725F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_099", CubeListBuilder.create().texOffs(96,192).addBox(-4.4600F, -21.3800F, -1.1150F, 0.5575F, 2.7875F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_100", CubeListBuilder.create().texOffs(128,192).addBox(-3.9025F, -19.7075F, -1.1150F, 0.5575F, 1.1150F, 1.6725F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_101", CubeListBuilder.create().texOffs(160,192).addBox(-4.4600F, -21.3800F, 0.0000F, 0.5575F, 2.7875F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_102", CubeListBuilder.create().texOffs(192,192).addBox(-4.4600F, -19.7075F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_103", CubeListBuilder.create().texOffs(224,192).addBox(-3.3450F, -19.7075F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_104", CubeListBuilder.create().texOffs(256,192).addBox(-4.4600F, -21.3800F, -1.6725F, 0.5575F, 2.2300F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_105", CubeListBuilder.create().texOffs(288,192).addBox(-3.9025F, -20.8225F, -1.6725F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_106", CubeListBuilder.create().texOffs(320,192).addBox(-3.9025F, -20.8225F, -1.1150F, 0.5575F, 1.1150F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_107", CubeListBuilder.create().texOffs(352,192).addBox(-3.9025F, -20.8225F, 0.0000F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_108", CubeListBuilder.create().texOffs(384,192).addBox(1.6725F, -8.5575F, -1.6725F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_109", CubeListBuilder.create().texOffs(416,192).addBox(2.2300F, -10.7875F, -1.6725F, 1.1150F, 2.7875F, 2.2300F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_110", CubeListBuilder.create().texOffs(448,192).addBox(3.3450F, -9.1150F, -1.6725F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_111", CubeListBuilder.create().texOffs(480,192).addBox(1.6725F, -8.5575F, -1.1150F, 0.5575F, 0.5575F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_112", CubeListBuilder.create().texOffs(0,224).addBox(3.3450F, -9.1150F, -1.1150F, 0.5575F, 1.1150F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_113", CubeListBuilder.create().texOffs(32,224).addBox(1.6725F, -8.5575F, 0.0000F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_114", CubeListBuilder.create().texOffs(64,224).addBox(3.3450F, -9.1150F, 0.0000F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_115", CubeListBuilder.create().texOffs(96,224).addBox(2.7875F, -9.1150F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_116", CubeListBuilder.create().texOffs(128,224).addBox(1.1150F, -14.6900F, -1.6725F, 0.5575F, 6.1325F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_117", CubeListBuilder.create().texOffs(160,224).addBox(1.6725F, -10.7875F, -1.6725F, 0.5575F, 2.2300F, 2.2300F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_118", CubeListBuilder.create().texOffs(192,224).addBox(1.1150F, -14.6900F, -1.1150F, 0.5575F, 6.1325F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_119", CubeListBuilder.create().texOffs(224,224).addBox(1.1150F, -14.6900F, 0.0000F, 0.5575F, 6.1325F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_120", CubeListBuilder.create().texOffs(256,224).addBox(1.1150F, -9.6725F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_121", CubeListBuilder.create().texOffs(288,224).addBox(2.2300F, -9.6725F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_122", CubeListBuilder.create().texOffs(320,224).addBox(3.9025F, -9.6725F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_123", CubeListBuilder.create().texOffs(352,224).addBox(3.3450F, -10.7875F, -1.6725F, 0.5575F, 1.6725F, 2.2300F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_124", CubeListBuilder.create().texOffs(384,224).addBox(3.9025F, -9.6725F, -1.6725F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_125", CubeListBuilder.create().texOffs(416,224).addBox(3.9025F, -9.6725F, -1.1150F, 0.5575F, 0.5575F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_126", CubeListBuilder.create().texOffs(448,224).addBox(3.9025F, -9.6725F, 0.0000F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_127", CubeListBuilder.create().texOffs(480,224).addBox(1.1150F, -10.7875F, -2.2300F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_128", CubeListBuilder.create().texOffs(0,256).addBox(1.6725F, -10.7875F, -2.2300F, 2.7875F, 1.1150F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_129", CubeListBuilder.create().texOffs(32,256).addBox(4.4600F, -10.7875F, -2.2300F, 0.5575F, 1.1150F, 1.1150F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_130", CubeListBuilder.create().texOffs(64,256).addBox(3.9025F, -10.7875F, -1.6725F, 0.5575F, 1.1150F, 2.2300F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_131", CubeListBuilder.create().texOffs(96,256).addBox(4.4600F, -19.7075F, -1.1150F, 0.5575F, 10.0350F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_132", CubeListBuilder.create().texOffs(128,256).addBox(4.4600F, -19.7075F, 0.0000F, 0.5575F, 10.0350F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_133", CubeListBuilder.create().texOffs(160,256).addBox(1.1150F, -12.4600F, -2.2300F, 0.5575F, 1.6725F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_134", CubeListBuilder.create().texOffs(192,256).addBox(1.6725F, -14.6900F, -2.2300F, 0.5575F, 3.9025F, 0.5575F),PartPose.ZERO); // inner_rose
        geo.addOrReplaceChild("rabbit_135", CubeListBuilder.create().texOffs(224,256).addBox(2.2300F, -11.9025F, -2.2300F, 1.6725F, 1.1150F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_136", CubeListBuilder.create().texOffs(256,256).addBox(3.9025F, -15.2475F, -2.2300F, 0.5575F, 4.4600F, 0.5575F),PartPose.ZERO); // inner_rose
        geo.addOrReplaceChild("rabbit_137", CubeListBuilder.create().texOffs(288,256).addBox(4.4600F, -12.4600F, -2.2300F, 0.5575F, 1.6725F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_138", CubeListBuilder.create().texOffs(320,256).addBox(1.6725F, -11.3450F, -1.6725F, 2.7875F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_139", CubeListBuilder.create().texOffs(352,256).addBox(4.4600F, -19.7075F, -1.6725F, 0.5575F, 8.9200F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_140", CubeListBuilder.create().texOffs(384,256).addBox(1.6725F, -14.6900F, -1.1150F, 2.7875F, 3.9025F, 1.6725F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_141", CubeListBuilder.create().texOffs(416,256).addBox(1.6725F, -13.5750F, -1.6725F, 2.7875F, 2.2300F, 0.5575F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_142", CubeListBuilder.create().texOffs(448,256).addBox(2.2300F, -14.6900F, -2.2300F, 0.5575F, 2.7875F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_143", CubeListBuilder.create().texOffs(480,256).addBox(2.7875F, -12.4600F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // inner_light
        geo.addOrReplaceChild("rabbit_144", CubeListBuilder.create().texOffs(0,288).addBox(3.3450F, -16.3625F, -2.2300F, 0.5575F, 4.4600F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_145", CubeListBuilder.create().texOffs(32,288).addBox(1.1150F, -13.5750F, -2.2300F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_146", CubeListBuilder.create().texOffs(64,288).addBox(2.7875F, -14.1325F, -2.2300F, 0.5575F, 1.6725F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_147", CubeListBuilder.create().texOffs(96,288).addBox(4.4600F, -13.5750F, -2.2300F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_148", CubeListBuilder.create().texOffs(128,288).addBox(1.1150F, -14.6900F, -2.2300F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_149", CubeListBuilder.create().texOffs(160,288).addBox(4.4600F, -15.2475F, -2.2300F, 0.5575F, 1.6725F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_150", CubeListBuilder.create().texOffs(192,288).addBox(1.6725F, -14.1325F, -1.6725F, 2.7875F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_151", CubeListBuilder.create().texOffs(224,288).addBox(2.7875F, -14.6900F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // inner_light
        geo.addOrReplaceChild("rabbit_152", CubeListBuilder.create().texOffs(256,288).addBox(1.6725F, -14.6900F, -1.6725F, 2.7875F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_153", CubeListBuilder.create().texOffs(288,288).addBox(1.6725F, -15.2475F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_154", CubeListBuilder.create().texOffs(320,288).addBox(2.2300F, -15.2475F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // inner_rose
        geo.addOrReplaceChild("rabbit_155", CubeListBuilder.create().texOffs(352,288).addBox(2.7875F, -17.4775F, -2.2300F, 0.5575F, 2.7875F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_156", CubeListBuilder.create().texOffs(384,288).addBox(1.6725F, -16.3625F, -1.6725F, 0.5575F, 1.6725F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_157", CubeListBuilder.create().texOffs(416,288).addBox(2.2300F, -16.3625F, -1.6725F, 2.2300F, 1.6725F, 2.2300F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_158", CubeListBuilder.create().texOffs(448,288).addBox(1.6725F, -16.3625F, -1.1150F, 0.5575F, 1.6725F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_159", CubeListBuilder.create().texOffs(480,288).addBox(1.6725F, -16.3625F, 0.0000F, 0.5575F, 1.6725F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_160", CubeListBuilder.create().texOffs(0,320).addBox(1.6725F, -16.3625F, -2.2300F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_161", CubeListBuilder.create().texOffs(32,320).addBox(2.2300F, -16.3625F, -2.2300F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_162", CubeListBuilder.create().texOffs(64,320).addBox(3.9025F, -18.0350F, -2.2300F, 0.5575F, 2.7875F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_163", CubeListBuilder.create().texOffs(96,320).addBox(4.4600F, -16.3625F, -2.2300F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_164", CubeListBuilder.create().texOffs(128,320).addBox(2.2300F, -17.4775F, -2.2300F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_165", CubeListBuilder.create().texOffs(160,320).addBox(3.3450F, -16.9200F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // inner_light
        geo.addOrReplaceChild("rabbit_166", CubeListBuilder.create().texOffs(192,320).addBox(4.4600F, -18.0350F, -2.2300F, 0.5575F, 1.6725F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_167", CubeListBuilder.create().texOffs(224,320).addBox(2.2300F, -17.4775F, -1.6725F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_168", CubeListBuilder.create().texOffs(256,320).addBox(2.7875F, -16.9200F, -1.6725F, 1.6725F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_169", CubeListBuilder.create().texOffs(288,320).addBox(2.2300F, -17.4775F, -1.1150F, 0.5575F, 1.1150F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_170", CubeListBuilder.create().texOffs(320,320).addBox(2.7875F, -17.4775F, -1.1150F, 1.6725F, 1.1150F, 1.6725F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_171", CubeListBuilder.create().texOffs(352,320).addBox(2.2300F, -17.4775F, 0.0000F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_172", CubeListBuilder.create().texOffs(384,320).addBox(3.3450F, -18.0350F, -2.2300F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // inner_pink
        geo.addOrReplaceChild("rabbit_173", CubeListBuilder.create().texOffs(416,320).addBox(2.7875F, -17.4775F, -1.6725F, 1.6725F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_174", CubeListBuilder.create().texOffs(448,320).addBox(2.7875F, -18.0350F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_175", CubeListBuilder.create().texOffs(480,320).addBox(2.7875F, -18.5925F, -1.6725F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_176", CubeListBuilder.create().texOffs(0,352).addBox(3.3450F, -18.5925F, -1.6725F, 1.1150F, 1.1150F, 2.2300F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_177", CubeListBuilder.create().texOffs(32,352).addBox(2.7875F, -18.5925F, -1.1150F, 0.5575F, 1.1150F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_178", CubeListBuilder.create().texOffs(64,352).addBox(2.7875F, -18.5925F, 0.0000F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_179", CubeListBuilder.create().texOffs(96,352).addBox(2.7875F, -18.5925F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_180", CubeListBuilder.create().texOffs(128,352).addBox(4.4600F, -19.1500F, -2.2300F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_181", CubeListBuilder.create().texOffs(160,352).addBox(3.3450F, -19.1500F, -2.2300F, 0.5575F, 0.5575F, 1.1150F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_182", CubeListBuilder.create().texOffs(192,352).addBox(3.9025F, -19.7075F, -1.6725F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_183", CubeListBuilder.create().texOffs(224,352).addBox(3.3450F, -21.3800F, -1.1150F, 0.5575F, 2.7875F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_184", CubeListBuilder.create().texOffs(256,352).addBox(3.9025F, -19.7075F, -1.1150F, 0.5575F, 1.1150F, 1.6725F),PartPose.ZERO); // outer_base
        geo.addOrReplaceChild("rabbit_185", CubeListBuilder.create().texOffs(288,352).addBox(3.3450F, -21.3800F, 0.0000F, 0.5575F, 2.7875F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_186", CubeListBuilder.create().texOffs(320,352).addBox(3.3450F, -19.7075F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_187", CubeListBuilder.create().texOffs(352,352).addBox(4.4600F, -19.7075F, -2.2300F, 0.5575F, 0.5575F, 0.5575F),PartPose.ZERO); // outer_light
        geo.addOrReplaceChild("rabbit_188", CubeListBuilder.create().texOffs(384,352).addBox(3.3450F, -21.3800F, -1.6725F, 0.5575F, 2.2300F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_189", CubeListBuilder.create().texOffs(416,352).addBox(3.9025F, -20.8225F, -1.6725F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        geo.addOrReplaceChild("rabbit_190", CubeListBuilder.create().texOffs(448,352).addBox(3.9025F, -20.8225F, -1.1150F, 0.5575F, 1.1150F, 1.1150F),PartPose.ZERO); // outer_dark
        geo.addOrReplaceChild("rabbit_191", CubeListBuilder.create().texOffs(480,352).addBox(3.9025F, -20.8225F, 0.0000F, 0.5575F, 1.1150F, 0.5575F),PartPose.ZERO); // outer_shadow
        return LayerDefinition.create(mesh, 512, 384);
    }
}
