package com.mdk.cosmetic.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

/**
 * Cat ears, generated from user's edited Cat_Ears_D.bbmodel (83 cuboids).
 * No vanilla helmet/hat cube: only the custom ears remain.
 * The Y axis is converted from Blockbench into Minecraft head coordinates.
 */
/** v1.17.1 scale and positioning calibration. */
public final class CatEarsModel extends HumanoidModel<LivingEntity> {
    public CatEarsModel() {
        super(createLayer().bakeRoot());
        this.setAllVisible(false);
        this.head.visible = true;
    }

    private static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("ear_000", CubeListBuilder.create().texOffs(6, 12).addBox(-5.4181F, -6.7735F, -2.8546F, 0.6711F, 1.0035F, 3.0295F), PartPose.ZERO); // left layer0_seg0
        head.addOrReplaceChild("ear_001", CubeListBuilder.create().texOffs(6, 12).addBox(-4.7189F, -6.7735F, -2.9245F, 0.6711F, 1.0035F, 3.1227F), PartPose.ZERO); // left layer0_seg1
        head.addOrReplaceChild("ear_002", CubeListBuilder.create().texOffs(6, 12).addBox(-4.0198F, -6.7735F, -2.9944F, 0.6711F, 1.0035F, 3.2159F), PartPose.ZERO); // left layer0_seg2
        head.addOrReplaceChild("ear_003", CubeListBuilder.create().texOffs(6, 12).addBox(-3.3208F, -6.7735F, -2.9944F, 0.6711F, 1.0035F, 3.2159F), PartPose.ZERO); // left layer0_seg3
        head.addOrReplaceChild("ear_004", CubeListBuilder.create().texOffs(6, 12).addBox(-2.6216F, -6.7735F, -2.9245F, 0.6711F, 1.0035F, 3.1227F), PartPose.ZERO); // left layer0_seg4
        head.addOrReplaceChild("ear_005", CubeListBuilder.create().texOffs(6, 12).addBox(-1.9225F, -6.7735F, -2.8546F, 0.6711F, 1.0035F, 3.0295F), PartPose.ZERO); // left layer0_seg5
        head.addOrReplaceChild("ear_006", CubeListBuilder.create().texOffs(70, 12).addBox(-5.1617F, -6.6230F, 0.1747F, 3.6820F, 0.8530F, 0.6408F), PartPose.ZERO); // left rearfill0
        head.addOrReplaceChild("ear_007", CubeListBuilder.create().texOffs(6, 12).addBox(-5.1850F, -7.6655F, -2.5634F, 0.5965F, 1.0035F, 2.5052F), PartPose.ZERO); // left layer1_seg0
        head.addOrReplaceChild("ear_008", CubeListBuilder.create().texOffs(6, 12).addBox(-4.5640F, -7.6655F, -2.6333F, 0.5965F, 1.0035F, 2.5983F), PartPose.ZERO); // left layer1_seg1
        head.addOrReplaceChild("ear_009", CubeListBuilder.create().texOffs(6, 12).addBox(-3.9417F, -7.6655F, -2.7032F, 0.5965F, 1.0035F, 2.6915F), PartPose.ZERO); // left layer1_seg2
        head.addOrReplaceChild("ear_010", CubeListBuilder.create().texOffs(6, 12).addBox(-3.3208F, -7.6655F, -2.7032F, 0.5965F, 1.0035F, 2.6915F), PartPose.ZERO); // left layer1_seg3
        head.addOrReplaceChild("ear_011", CubeListBuilder.create().texOffs(6, 12).addBox(-2.6997F, -7.6655F, -2.6333F, 0.5965F, 1.0035F, 2.5983F), PartPose.ZERO); // left layer1_seg4
        head.addOrReplaceChild("ear_012", CubeListBuilder.create().texOffs(6, 12).addBox(-2.0775F, -7.6655F, -2.5634F, 0.5965F, 1.0035F, 2.5052F), PartPose.ZERO); // left layer1_seg5
        head.addOrReplaceChild("ear_013", CubeListBuilder.create().texOffs(70, 12).addBox(-4.9287F, -7.5150F, -0.0582F, 3.2159F, 0.8530F, 0.6408F), PartPose.ZERO); // left rearfill1
        head.addOrReplaceChild("ear_014", CubeListBuilder.create().texOffs(6, 12).addBox(-4.9520F, -8.5575F, -2.3070F, 0.6269F, 1.0035F, 2.0274F), PartPose.ZERO); // left layer2_seg0
        head.addOrReplaceChild("ear_015", CubeListBuilder.create().texOffs(70, 12).addBox(-4.2994F, -8.5575F, -2.3770F, 0.6269F, 1.0035F, 2.1206F), PartPose.ZERO); // left layer2_seg1
        head.addOrReplaceChild("ear_016", CubeListBuilder.create().texOffs(70, 12).addBox(-3.6469F, -8.5575F, -2.4469F, 0.6269F, 1.0035F, 2.2138F), PartPose.ZERO); // left layer2_seg2
        head.addOrReplaceChild("ear_017", CubeListBuilder.create().texOffs(70, 12).addBox(-2.9944F, -8.5575F, -2.3770F, 0.6269F, 1.0035F, 2.1206F), PartPose.ZERO); // left layer2_seg3
        head.addOrReplaceChild("ear_018", CubeListBuilder.create().texOffs(6, 12).addBox(-2.3419F, -8.5575F, -2.3070F, 0.6269F, 1.0035F, 2.0274F), PartPose.ZERO); // left layer2_seg4
        head.addOrReplaceChild("ear_019", CubeListBuilder.create().texOffs(70, 12).addBox(-4.6957F, -8.4070F, -0.2796F, 2.7498F, 0.8530F, 0.6408F), PartPose.ZERO); // left rearfill2
        head.addOrReplaceChild("ear_020", CubeListBuilder.create().texOffs(6, 12).addBox(-4.7189F, -9.4495F, -2.0507F, 0.6711F, 1.0035F, 1.4914F), PartPose.ZERO); // left layer3_seg0
        head.addOrReplaceChild("ear_021", CubeListBuilder.create().texOffs(70, 12).addBox(-4.0198F, -9.4495F, -2.1206F, 0.6711F, 1.0035F, 1.5846F), PartPose.ZERO); // left layer3_seg1
        head.addOrReplaceChild("ear_022", CubeListBuilder.create().texOffs(70, 12).addBox(-3.3208F, -9.4495F, -2.1206F, 0.6711F, 1.0035F, 1.5846F), PartPose.ZERO); // left layer3_seg2
        head.addOrReplaceChild("ear_023", CubeListBuilder.create().texOffs(6, 12).addBox(-2.6216F, -9.4495F, -2.0507F, 0.6711F, 1.0035F, 1.4914F), PartPose.ZERO); // left layer3_seg3
        head.addOrReplaceChild("ear_024", CubeListBuilder.create().texOffs(70, 12).addBox(-4.4626F, -9.2990F, -0.5593F, 2.2837F, 0.8530F, 0.6408F), PartPose.ZERO); // left rearfill3
        head.addOrReplaceChild("ear_025", CubeListBuilder.create().texOffs(6, 12).addBox(-4.3694F, -10.2300F, -1.7361F, 0.6711F, 0.8920F, 0.8389F), PartPose.ZERO); // left layer4_seg0
        head.addOrReplaceChild("ear_026", CubeListBuilder.create().texOffs(70, 12).addBox(-3.6702F, -10.2300F, -1.8061F, 0.6711F, 0.8920F, 0.9321F), PartPose.ZERO); // left layer4_seg1
        head.addOrReplaceChild("ear_027", CubeListBuilder.create().texOffs(6, 12).addBox(-2.9711F, -10.2300F, -1.7361F, 0.6711F, 0.8920F, 0.8389F), PartPose.ZERO); // left layer4_seg2
        head.addOrReplaceChild("ear_028", CubeListBuilder.create().texOffs(6, 12).addBox(-3.9616F, -10.8990F, -1.4215F, 0.6153F, 0.6690F, 0.2446F), PartPose.ZERO); // left layer5_seg0
        head.addOrReplaceChild("ear_029", CubeListBuilder.create().texOffs(6, 12).addBox(-3.3208F, -10.8990F, -1.4215F, 0.6153F, 0.6690F, 0.2446F), PartPose.ZERO); // left layer5_seg1
        head.addOrReplaceChild("ear_030", CubeListBuilder.create().texOffs(198, 12).addBox(-4.5442F, -6.9407F, -2.2720F, 0.5744F, 0.7805F, 1.7478F), PartPose.ZERO); // left inner0_0
        head.addOrReplaceChild("ear_031", CubeListBuilder.create().texOffs(198, 12).addBox(-3.9325F, -6.9407F, -2.2720F, 0.5756F, 0.7805F, 1.7478F), PartPose.ZERO); // left inner0_1
        head.addOrReplaceChild("ear_032", CubeListBuilder.create().texOffs(198, 12).addBox(-3.3208F, -6.9407F, -2.2720F, 0.5744F, 0.7805F, 1.7478F), PartPose.ZERO); // left inner0_2
        head.addOrReplaceChild("ear_033", CubeListBuilder.create().texOffs(198, 12).addBox(-2.7090F, -6.9407F, -2.2720F, 0.5756F, 0.7805F, 1.7478F), PartPose.ZERO); // left inner0_3
        head.addOrReplaceChild("ear_034", CubeListBuilder.create().texOffs(198, 12).addBox(-4.3111F, -7.7770F, -1.9808F, 0.6211F, 0.7805F, 1.2234F), PartPose.ZERO); // left inner1_0
        head.addOrReplaceChild("ear_035", CubeListBuilder.create().texOffs(198, 12).addBox(-3.6505F, -7.7770F, -1.9808F, 0.6198F, 0.7805F, 1.2234F), PartPose.ZERO); // left inner1_1
        head.addOrReplaceChild("ear_036", CubeListBuilder.create().texOffs(198, 12).addBox(-2.9910F, -7.7770F, -1.9808F, 0.6211F, 0.7805F, 1.2234F), PartPose.ZERO); // left inner1_2
        head.addOrReplaceChild("ear_037", CubeListBuilder.create().texOffs(262, 12).addBox(-4.1072F, -8.6133F, -1.6894F, 0.7387F, 0.7805F, 0.6991F), PartPose.ZERO); // left inner2_0
        head.addOrReplaceChild("ear_038", CubeListBuilder.create().texOffs(262, 12).addBox(-3.3208F, -8.6133F, -1.6894F, 0.7387F, 0.7805F, 0.6991F), PartPose.ZERO); // left inner2_1
        head.addOrReplaceChild("ear_039", CubeListBuilder.create().texOffs(262, 12).addBox(-3.8451F, -9.3380F, -1.3982F, 0.9858F, 0.6690F, 0.2330F), PartPose.ZERO); // left inner3_0
        head.addOrReplaceChild("ear_040", CubeListBuilder.create().texOffs(326, 12).addBox(-3.5304F, -10.5087F, -1.2351F, 0.4195F, 0.3345F, 0.2563F), PartPose.ZERO); // left tip_highlight
        head.addOrReplaceChild("ear_041", CubeListBuilder.create().texOffs(6, 12).addBox(4.7469F, -6.7735F, -2.8546F, 0.6711F, 1.0035F, 3.0295F), PartPose.ZERO); // right layer0_seg0
        head.addOrReplaceChild("ear_042", CubeListBuilder.create().texOffs(6, 12).addBox(4.0478F, -6.7735F, -2.9245F, 0.6711F, 1.0035F, 3.1227F), PartPose.ZERO); // right layer0_seg1
        head.addOrReplaceChild("ear_043", CubeListBuilder.create().texOffs(6, 12).addBox(3.3487F, -6.7735F, -2.9944F, 0.6711F, 1.0035F, 3.2159F), PartPose.ZERO); // right layer0_seg2
        head.addOrReplaceChild("ear_044", CubeListBuilder.create().texOffs(6, 12).addBox(2.6496F, -6.7735F, -2.9944F, 0.6711F, 1.0035F, 3.2159F), PartPose.ZERO); // right layer0_seg3
        head.addOrReplaceChild("ear_045", CubeListBuilder.create().texOffs(6, 12).addBox(1.9505F, -6.7735F, -2.9245F, 0.6711F, 1.0035F, 3.1227F), PartPose.ZERO); // right layer0_seg4
        head.addOrReplaceChild("ear_046", CubeListBuilder.create().texOffs(6, 12).addBox(1.2514F, -6.7735F, -2.8546F, 0.6711F, 1.0035F, 3.0295F), PartPose.ZERO); // right layer0_seg5
        head.addOrReplaceChild("ear_047", CubeListBuilder.create().texOffs(70, 12).addBox(1.4797F, -6.6230F, 0.1747F, 3.6820F, 0.8530F, 0.6408F), PartPose.ZERO); // right rearfill0
        head.addOrReplaceChild("ear_048", CubeListBuilder.create().texOffs(6, 12).addBox(4.5884F, -7.6655F, -2.5634F, 0.5965F, 1.0035F, 2.5052F), PartPose.ZERO); // right layer1_seg0
        head.addOrReplaceChild("ear_049", CubeListBuilder.create().texOffs(6, 12).addBox(3.9674F, -7.6655F, -2.6333F, 0.5965F, 1.0035F, 2.5983F), PartPose.ZERO); // right layer1_seg1
        head.addOrReplaceChild("ear_050", CubeListBuilder.create().texOffs(6, 12).addBox(3.3452F, -7.6655F, -2.7032F, 0.5965F, 1.0035F, 2.6915F), PartPose.ZERO); // right layer1_seg2
        head.addOrReplaceChild("ear_051", CubeListBuilder.create().texOffs(6, 12).addBox(2.7242F, -7.6655F, -2.7032F, 0.5965F, 1.0035F, 2.6915F), PartPose.ZERO); // right layer1_seg3
        head.addOrReplaceChild("ear_052", CubeListBuilder.create().texOffs(6, 12).addBox(2.1031F, -7.6655F, -2.6333F, 0.5965F, 1.0035F, 2.5983F), PartPose.ZERO); // right layer1_seg4
        head.addOrReplaceChild("ear_053", CubeListBuilder.create().texOffs(6, 12).addBox(1.4809F, -7.6655F, -2.5634F, 0.5965F, 1.0035F, 2.5052F), PartPose.ZERO); // right layer1_seg5
        head.addOrReplaceChild("ear_054", CubeListBuilder.create().texOffs(70, 12).addBox(1.7128F, -7.5150F, -0.0582F, 3.2159F, 0.8530F, 0.6408F), PartPose.ZERO); // right rearfill1
        head.addOrReplaceChild("ear_055", CubeListBuilder.create().texOffs(6, 12).addBox(4.3251F, -8.5575F, -2.3070F, 0.6269F, 1.0035F, 2.0274F), PartPose.ZERO); // right layer2_seg0
        head.addOrReplaceChild("ear_056", CubeListBuilder.create().texOffs(70, 12).addBox(3.6726F, -8.5575F, -2.3770F, 0.6269F, 1.0035F, 2.1206F), PartPose.ZERO); // right layer2_seg1
        head.addOrReplaceChild("ear_057", CubeListBuilder.create().texOffs(70, 12).addBox(3.0201F, -8.5575F, -2.4469F, 0.6269F, 1.0035F, 2.2138F), PartPose.ZERO); // right layer2_seg2
        head.addOrReplaceChild("ear_058", CubeListBuilder.create().texOffs(70, 12).addBox(2.3676F, -8.5575F, -2.3770F, 0.6269F, 1.0035F, 2.1206F), PartPose.ZERO); // right layer2_seg3
        head.addOrReplaceChild("ear_059", CubeListBuilder.create().texOffs(6, 12).addBox(1.7151F, -8.5575F, -2.3070F, 0.6269F, 1.0035F, 2.0274F), PartPose.ZERO); // right layer2_seg4
        head.addOrReplaceChild("ear_060", CubeListBuilder.create().texOffs(70, 12).addBox(1.9458F, -8.4070F, -0.2796F, 2.7498F, 0.8530F, 0.6408F), PartPose.ZERO); // right rearfill2
        head.addOrReplaceChild("ear_061", CubeListBuilder.create().texOffs(6, 12).addBox(4.0478F, -9.4495F, -2.0507F, 0.6711F, 1.0035F, 1.4914F), PartPose.ZERO); // right layer3_seg0
        head.addOrReplaceChild("ear_062", CubeListBuilder.create().texOffs(70, 12).addBox(3.3487F, -9.4495F, -2.1206F, 0.6711F, 1.0035F, 1.5846F), PartPose.ZERO); // right layer3_seg1
        head.addOrReplaceChild("ear_063", CubeListBuilder.create().texOffs(70, 12).addBox(2.6496F, -9.4495F, -2.1206F, 0.6711F, 1.0035F, 1.5846F), PartPose.ZERO); // right layer3_seg2
        head.addOrReplaceChild("ear_064", CubeListBuilder.create().texOffs(6, 12).addBox(1.9505F, -9.4495F, -2.0507F, 0.6711F, 1.0035F, 1.4914F), PartPose.ZERO); // right layer3_seg3
        head.addOrReplaceChild("ear_065", CubeListBuilder.create().texOffs(70, 12).addBox(2.1789F, -9.2990F, -0.5593F, 2.2837F, 0.8530F, 0.6408F), PartPose.ZERO); // right rearfill3
        head.addOrReplaceChild("ear_066", CubeListBuilder.create().texOffs(6, 12).addBox(3.6982F, -10.2300F, -1.7361F, 0.6711F, 0.8920F, 0.8389F), PartPose.ZERO); // right layer4_seg0
        head.addOrReplaceChild("ear_067", CubeListBuilder.create().texOffs(70, 12).addBox(2.9991F, -10.2300F, -1.8061F, 0.6711F, 0.8920F, 0.9321F), PartPose.ZERO); // right layer4_seg1
        head.addOrReplaceChild("ear_068", CubeListBuilder.create().texOffs(6, 12).addBox(2.3000F, -10.2300F, -1.7361F, 0.6711F, 0.8920F, 0.8389F), PartPose.ZERO); // right layer4_seg2
        head.addOrReplaceChild("ear_069", CubeListBuilder.create().texOffs(6, 12).addBox(3.3463F, -10.8990F, -1.4215F, 0.6153F, 0.6690F, 0.2446F), PartPose.ZERO); // right layer5_seg0
        head.addOrReplaceChild("ear_070", CubeListBuilder.create().texOffs(6, 12).addBox(2.7055F, -10.8990F, -1.4215F, 0.6153F, 0.6690F, 0.2446F), PartPose.ZERO); // right layer5_seg1
        head.addOrReplaceChild("ear_071", CubeListBuilder.create().texOffs(6, 12).addBox(1.0487F, -6.4390F, -2.0973F, 0.4661F, 0.6690F, 0.6409F), PartPose.ZERO); // right rim_outerR_0
        head.addOrReplaceChild("ear_072", CubeListBuilder.create().texOffs(198, 12).addBox(3.9697F, -6.9407F, -2.2720F, 0.5744F, 0.7805F, 1.7478F), PartPose.ZERO); // right inner0_0
        head.addOrReplaceChild("ear_073", CubeListBuilder.create().texOffs(198, 12).addBox(3.3568F, -6.9407F, -2.2720F, 0.5756F, 0.7805F, 1.7478F), PartPose.ZERO); // right inner0_1
        head.addOrReplaceChild("ear_074", CubeListBuilder.create().texOffs(198, 12).addBox(2.7464F, -6.9407F, -2.2720F, 0.5744F, 0.7805F, 1.7478F), PartPose.ZERO); // right inner0_2
        head.addOrReplaceChild("ear_075", CubeListBuilder.create().texOffs(198, 12).addBox(2.1334F, -6.9407F, -2.2720F, 0.5756F, 0.7805F, 1.7478F), PartPose.ZERO); // right inner0_3
        head.addOrReplaceChild("ear_076", CubeListBuilder.create().texOffs(198, 12).addBox(3.6901F, -7.7770F, -1.9808F, 0.6211F, 0.7805F, 1.2234F), PartPose.ZERO); // right inner1_0
        head.addOrReplaceChild("ear_077", CubeListBuilder.create().texOffs(198, 12).addBox(3.0306F, -7.7770F, -1.9808F, 0.6198F, 0.7805F, 1.2234F), PartPose.ZERO); // right inner1_1
        head.addOrReplaceChild("ear_078", CubeListBuilder.create().texOffs(198, 12).addBox(2.3699F, -7.7770F, -1.9808F, 0.6211F, 0.7805F, 1.2234F), PartPose.ZERO); // right inner1_2
        head.addOrReplaceChild("ear_079", CubeListBuilder.create().texOffs(262, 12).addBox(3.3685F, -8.6133F, -1.6894F, 0.7387F, 0.7805F, 0.6991F), PartPose.ZERO); // right inner2_0
        head.addOrReplaceChild("ear_080", CubeListBuilder.create().texOffs(262, 12).addBox(2.5820F, -8.6133F, -1.6894F, 0.7387F, 0.7805F, 0.6991F), PartPose.ZERO); // right inner2_1
        head.addOrReplaceChild("ear_081", CubeListBuilder.create().texOffs(262, 12).addBox(2.8593F, -9.3380F, -1.3982F, 0.9858F, 0.6690F, 0.2330F), PartPose.ZERO); // right inner3_0
        head.addOrReplaceChild("ear_082", CubeListBuilder.create().texOffs(326, 12).addBox(3.1110F, -10.5087F, -1.2351F, 0.4195F, 0.3345F, 0.2563F), PartPose.ZERO); // right tip_highlight
        return LayerDefinition.create(mesh, 512, 128);
    }
}
