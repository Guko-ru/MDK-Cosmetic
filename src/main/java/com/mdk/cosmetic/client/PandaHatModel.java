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
 * Panda cap geometry generated from the player-edited Panda_Hat_Ref.bbmodel.
 * The original model is included in blockbench/ and is NOT modified.
 * Y axis is inverted relative to Blockbench around a head pivot at Y=24.
 * The vanilla outer head overlay and vanilla head armor cubes are removed.
 */
/** v1.17.1 scale and positioning calibration. */
public final class PandaHatModel extends HumanoidModel<LivingEntity> {
    public PandaHatModel() {
        super(createLayer().bakeRoot());
        this.setAllVisible(false);
        this.head.visible = true;
    }

    private static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("panda_part_00", CubeListBuilder.create().texOffs(6, 12).addBox(-4.0548F, -8.8697F, -4.1713F, 8.1096F, 0.5575F, 8.3427F), PartPose.ZERO); // Upper soft crown
        head.addOrReplaceChild("panda_part_01", CubeListBuilder.create().texOffs(70, 12).addBox(-4.3694F, -8.4349F, -4.7189F, 8.7388F, 0.6690F, 1.1302F), PartPose.ZERO); // Soft front-top bevel
        head.addOrReplaceChild("panda_part_02", CubeListBuilder.create().texOffs(134, 12).addBox(-4.3811F, -8.4349F, 3.5887F, 8.7621F, 0.6913F, 1.1419F), PartPose.ZERO); // Soft back-top bevel
        head.addOrReplaceChild("panda_part_03", CubeListBuilder.create().texOffs(134, 12).addBox(-4.7189F, -8.4460F, -4.1363F, 0.9321F, 0.6802F, 8.2727F), PartPose.ZERO); // Soft left-top bevel
        head.addOrReplaceChild("panda_part_04", CubeListBuilder.create().texOffs(6, 12).addBox(3.7868F, -8.4460F, -4.1363F, 0.9321F, 0.6802F, 8.2727F), PartPose.ZERO); // Soft right-top bevel
        head.addOrReplaceChild("panda_part_05", CubeListBuilder.create().texOffs(6, 12).addBox(-5.0103F, -8.1450F, -4.1946F, 1.2234F, 1.4272F, 8.4126F), PartPose.ZERO); // L rounded upper shoulder
        head.addOrReplaceChild("panda_part_06", CubeListBuilder.create().texOffs(134, 12).addBox(-5.3132F, -7.0188F, -4.0198F, 0.5942F, 1.5721F, 8.0397F), PartPose.ZERO); // L mid shoulder
        head.addOrReplaceChild("panda_part_07", CubeListBuilder.create().texOffs(6, 12).addBox(-5.0685F, -7.7993F, -4.8820F, 0.6408F, 1.9847F, 1.1186F), PartPose.ZERO); // L front curved edge
        head.addOrReplaceChild("panda_part_08", CubeListBuilder.create().texOffs(134, 12).addBox(-5.0452F, -7.7993F, 3.8684F, 0.7923F, 2.2969F, 1.0021F), PartPose.ZERO); // L back curved edge
        head.addOrReplaceChild("panda_part_09", CubeListBuilder.create().texOffs(6, 12).addBox(3.7868F, -8.1450F, -4.1946F, 1.2234F, 1.4272F, 8.4126F), PartPose.ZERO); // R rounded upper shoulder
        head.addOrReplaceChild("panda_part_10", CubeListBuilder.create().texOffs(134, 12).addBox(4.7189F, -7.0188F, -4.0198F, 0.5942F, 1.5721F, 8.0397F), PartPose.ZERO); // R mid shoulder
        head.addOrReplaceChild("panda_part_11", CubeListBuilder.create().texOffs(6, 12).addBox(4.4277F, -7.7993F, -4.8820F, 0.6408F, 1.9847F, 1.1186F), PartPose.ZERO); // R front curved edge
        head.addOrReplaceChild("panda_part_12", CubeListBuilder.create().texOffs(134, 12).addBox(4.2528F, -7.7993F, 3.8684F, 0.7923F, 2.2969F, 1.0021F), PartPose.ZERO); // R back curved edge
        head.addOrReplaceChild("panda_part_13", CubeListBuilder.create().texOffs(70, 12).addBox(-4.3345F, -7.9219F, -5.3132F, 8.6689F, 1.7617F, 0.7573F), PartPose.ZERO); // Broad creamy forehead band
        head.addOrReplaceChild("panda_part_14", CubeListBuilder.create().texOffs(6, 12).addBox(-3.5887F, -8.1561F, -5.4646F, 7.1775F, 0.5241F, 0.8622F), PartPose.ZERO); // Raised forehead ridge
        head.addOrReplaceChild("panda_part_15", CubeListBuilder.create().texOffs(134, 12).addBox(-4.7539F, -7.7770F, -5.1501F, 1.1186F, 1.1484F, 0.6991F), PartPose.ZERO); // Forehead left upper step
        head.addOrReplaceChild("panda_part_16", CubeListBuilder.create().texOffs(134, 12).addBox(3.6353F, -7.7770F, -5.1501F, 1.1186F, 1.1484F, 0.6991F), PartPose.ZERO); // Forehead right upper step
        head.addOrReplaceChild("panda_part_17", CubeListBuilder.create().texOffs(70, 12).addBox(-2.0507F, -6.5951F, -5.5695F, 4.1014F, 2.0070F, 0.7108F), PartPose.ZERO); // White center between eyes
        head.addOrReplaceChild("panda_part_18", CubeListBuilder.create().texOffs(134, 12).addBox(-4.2179F, -6.3052F, -5.3947F, 2.6449F, 2.4530F, 0.7108F), PartPose.ZERO); // Left white cheek backdrop
        head.addOrReplaceChild("panda_part_19", CubeListBuilder.create().texOffs(134, 12).addBox(1.5729F, -6.3052F, -5.3947F, 2.6449F, 2.4530F, 0.7108F), PartPose.ZERO); // Right white cheek backdrop
        head.addOrReplaceChild("panda_part_20", CubeListBuilder.create().texOffs(390, 12).addBox(-4.0082F, -6.5728F, -5.8143F, 1.4215F, 0.8586F, 0.6176F), PartPose.ZERO); // Left black eyebrow
        head.addOrReplaceChild("panda_part_21", CubeListBuilder.create().texOffs(390, 12).addBox(-4.0082F, -5.9261F, -5.9890F, 2.0973F, 2.1073F, 0.7341F), PartPose.ZERO); // Left main black eye mask
        head.addOrReplaceChild("panda_part_22", CubeListBuilder.create().texOffs(6, 12).addBox(-3.7985F, -4.2424F, -5.9540F, 1.6195F, 1.1819F, 0.7457F), PartPose.ZERO); // Left panda eye lower patch
        head.addOrReplaceChild("panda_part_23", CubeListBuilder.create().texOffs(390, 12).addBox(-4.5558F, -5.2125F, -5.8492F, 1.2001F, 2.1965F, 0.9321F), PartPose.ZERO); // Left corner black cheek
        head.addOrReplaceChild("panda_part_24", CubeListBuilder.create().texOffs(326, 12).addBox(-2.8896F, -5.5247F, -6.0589F, 0.5360F, 0.4237F, 0.1456F), PartPose.ZERO); // Left subtle eye glint
        head.addOrReplaceChild("panda_part_25", CubeListBuilder.create().texOffs(390, 12).addBox(2.5867F, -6.5728F, -5.8143F, 1.4215F, 0.8586F, 0.6176F), PartPose.ZERO); // Right black eyebrow
        head.addOrReplaceChild("panda_part_26", CubeListBuilder.create().texOffs(390, 12).addBox(1.9109F, -5.9261F, -5.9890F, 2.0973F, 2.1073F, 0.7341F), PartPose.ZERO); // Right main black eye mask
        head.addOrReplaceChild("panda_part_27", CubeListBuilder.create().texOffs(6, 12).addBox(2.1789F, -4.2424F, -5.9540F, 1.6195F, 1.1819F, 0.7457F), PartPose.ZERO); // Right panda eye lower patch
        head.addOrReplaceChild("panda_part_28", CubeListBuilder.create().texOffs(390, 12).addBox(3.3557F, -5.2125F, -5.8492F, 1.2001F, 2.1965F, 0.9321F), PartPose.ZERO); // Right corner black cheek
        head.addOrReplaceChild("panda_part_29", CubeListBuilder.create().texOffs(326, 12).addBox(2.3537F, -5.5247F, -6.0589F, 0.5360F, 0.4237F, 0.1456F), PartPose.ZERO); // Right subtle eye glint
        head.addOrReplaceChild("panda_part_30", CubeListBuilder.create().texOffs(70, 12).addBox(-2.1905F, -4.9895F, -6.2686F, 4.3811F, 0.9477F, 0.6875F), PartPose.ZERO); // Front white muzzle top
        head.addOrReplaceChild("panda_part_31", CubeListBuilder.create().texOffs(70, 12).addBox(-2.4702F, -4.1756F, -6.2802F, 4.9403F, 1.0481F, 0.6175F), PartPose.ZERO); // Front white snout lower
        head.addOrReplaceChild("panda_part_32", CubeListBuilder.create().texOffs(6, 12).addBox(-1.8177F, -3.2166F, -6.1172F, 3.6353F, 0.4237F, 0.4079F), PartPose.ZERO); // Front white snout lower edge
        head.addOrReplaceChild("panda_part_33", CubeListBuilder.create().texOffs(390, 12).addBox(-0.5476F, -5.0564F, -6.5949F, 1.0953F, 0.6802F, 0.4312F), PartPose.ZERO); // Black square panda nose
        head.addOrReplaceChild("panda_part_34", CubeListBuilder.create().texOffs(390, 12).addBox(-5.4996F, -5.4690F, -4.2179F, 0.6525F, 2.1965F, 8.5407F), PartPose.ZERO); // Left dark side band upper
        head.addOrReplaceChild("panda_part_35", CubeListBuilder.create().texOffs(6, 12).addBox(-5.6277F, -3.9303F, -3.9965F, 0.9088F, 1.9178F, 7.8999F), PartPose.ZERO); // Left dark lower side band
        head.addOrReplaceChild("panda_part_36", CubeListBuilder.create().texOffs(390, 12).addBox(-5.7676F, -3.4843F, -5.1035F, 0.7690F, 2.4865F, 2.4119F), PartPose.ZERO); // Left hanging front flap
        head.addOrReplaceChild("panda_part_37", CubeListBuilder.create().texOffs(454, 12).addBox(-5.5112F, -4.9115F, -5.7210F, 0.8389F, 3.2781F, 1.0254F), PartPose.ZERO); // Left front side cheek border
        head.addOrReplaceChild("panda_part_38", CubeListBuilder.create().texOffs(390, 12).addBox(-5.3598F, -3.3059F, -5.5928F, 1.0370F, 1.0481F, 0.7107F), PartPose.ZERO); // Left small front tip
        head.addOrReplaceChild("panda_part_39", CubeListBuilder.create().texOffs(326, 12).addBox(-5.5229F, -2.4250F, 2.2138F, 0.4545F, 0.6021F, 2.0157F), PartPose.ZERO); // Left side trailing lower seam
        head.addOrReplaceChild("panda_part_40", CubeListBuilder.create().texOffs(390, 12).addBox(4.8471F, -5.4690F, -4.2179F, 0.6525F, 2.1965F, 8.5407F), PartPose.ZERO); // Right dark side band upper
        head.addOrReplaceChild("panda_part_41", CubeListBuilder.create().texOffs(6, 12).addBox(4.7189F, -3.9303F, -3.9965F, 0.9088F, 1.9178F, 7.8999F), PartPose.ZERO); // Right dark lower side band
        head.addOrReplaceChild("panda_part_42", CubeListBuilder.create().texOffs(390, 12).addBox(4.9985F, -3.4843F, -5.1035F, 0.7690F, 2.4865F, 2.4119F), PartPose.ZERO); // Right hanging front flap
        head.addOrReplaceChild("panda_part_43", CubeListBuilder.create().texOffs(454, 12).addBox(4.6723F, -4.9115F, -5.7210F, 0.8389F, 3.2781F, 1.0254F), PartPose.ZERO); // Right front side cheek border
        head.addOrReplaceChild("panda_part_44", CubeListBuilder.create().texOffs(390, 12).addBox(4.3227F, -3.3059F, -5.5928F, 1.0370F, 1.0481F, 0.7107F), PartPose.ZERO); // Right small front tip
        head.addOrReplaceChild("panda_part_45", CubeListBuilder.create().texOffs(326, 12).addBox(5.0685F, -2.4250F, 2.2138F, 0.4545F, 0.6021F, 2.0157F), PartPose.ZERO); // Right side trailing lower seam
        head.addOrReplaceChild("panda_part_46", CubeListBuilder.create().texOffs(6, 12).addBox(-4.3460F, -7.9331F, 4.3111F, 8.6922F, 2.5422F, 0.8738F), PartPose.ZERO); // Back white upper veil
        head.addOrReplaceChild("panda_part_47", CubeListBuilder.create().texOffs(70, 12).addBox(-3.9150F, -8.2676F, 4.3345F, 7.8300F, 0.5798F, 0.5360F), PartPose.ZERO); // Back rounded broad top
        head.addOrReplaceChild("panda_part_48", CubeListBuilder.create().texOffs(390, 12).addBox(-4.5908F, -5.3798F, 4.8471F, 9.1816F, 2.5756F, 0.8738F), PartPose.ZERO); // Back black bottom border
        head.addOrReplaceChild("panda_part_49", CubeListBuilder.create().texOffs(454, 12).addBox(-4.2295F, -3.0828F, 5.0103F, 8.4592F, 0.4794F, 0.7108F), PartPose.ZERO); // Back lower hem central shadow
        head.addOrReplaceChild("panda_part_50", CubeListBuilder.create().texOffs(6, 12).addBox(-5.4413F, -5.2794F, 4.2062F, 1.3399F, 2.9213F, 1.3749F), PartPose.ZERO); // Left back border tab
        head.addOrReplaceChild("panda_part_51", CubeListBuilder.create().texOffs(6, 12).addBox(4.1014F, -5.2794F, 4.2062F, 1.3399F, 2.9213F, 1.3749F), PartPose.ZERO); // Right back border tab
        head.addOrReplaceChild("panda_part_52", CubeListBuilder.create().texOffs(198, 12).addBox(-4.4510F, -8.0334F, -4.4393F, 8.9019F, 0.2899F, 8.8436F), PartPose.ZERO); // Inner crown gray pad
        head.addOrReplaceChild("panda_part_53", CubeListBuilder.create().texOffs(262, 12).addBox(-4.3345F, -6.0934F, -4.8704F, 8.6689F, 0.3457F, 0.3845F), PartPose.ZERO); // Inner front dark seam
        head.addOrReplaceChild("panda_part_54", CubeListBuilder.create().texOffs(326, 12).addBox(-4.7306F, -5.5470F, -4.2412F, 0.2912F, 2.1965F, 8.5174F), PartPose.ZERO); // Left inner soft lining
        head.addOrReplaceChild("panda_part_55", CubeListBuilder.create().texOffs(326, 12).addBox(4.4393F, -5.5470F, -4.2412F, 0.2912F, 2.1965F, 8.5174F), PartPose.ZERO); // Right inner soft lining
        head.addOrReplaceChild("panda_part_56", CubeListBuilder.create().texOffs(390, 12).addBox(-4.7422F, -9.3603F, -3.3324F, 2.3770F, 0.8586F, 2.9828F), PartPose.ZERO); // Left ear rounded bottom shell
        head.addOrReplaceChild("panda_part_57", CubeListBuilder.create().texOffs(390, 12).addBox(-4.7422F, -10.5087F, -3.4489F, 2.3770F, 1.2599F, 3.0179F), PartPose.ZERO); // Left ear outer body
        head.addOrReplaceChild("panda_part_58", CubeListBuilder.create().texOffs(6, 12).addBox(-4.3927F, -10.9548F, -3.1460F, 1.7129F, 0.5241F, 2.4818F), PartPose.ZERO); // Left ear rounded top
        head.addOrReplaceChild("panda_part_59", CubeListBuilder.create().texOffs(134, 12).addBox(-4.3111F, -10.4976F, -3.5887F, 1.5030F, 1.2377F, 0.1864F), PartPose.ZERO); // Left ear recessed lining
        head.addOrReplaceChild("panda_part_60", CubeListBuilder.create().texOffs(198, 12).addBox(-4.1829F, -10.3638F, -3.6586F, 1.2001F, 0.4014F, 0.1573F), PartPose.ZERO); // Left ear inner bright top
        head.addOrReplaceChild("panda_part_61", CubeListBuilder.create().texOffs(454, 12).addBox(-4.5675F, -10.3415F, -0.5826F, 2.0158F, 1.0927F, 0.5593F), PartPose.ZERO); // Left ear back bulge
        head.addOrReplaceChild("panda_part_62", CubeListBuilder.create().texOffs(454, 12).addBox(-4.3345F, -11.0885F, -2.8546F, 1.5962F, 0.2119F, 1.9458F), PartPose.ZERO); // Left ear top black edge
        head.addOrReplaceChild("panda_part_63", CubeListBuilder.create().texOffs(390, 12).addBox(2.3652F, -9.3603F, -3.3324F, 2.3770F, 0.8586F, 2.9828F), PartPose.ZERO); // Right ear rounded bottom shell
        head.addOrReplaceChild("panda_part_64", CubeListBuilder.create().texOffs(390, 12).addBox(2.3652F, -10.5087F, -3.4489F, 2.3770F, 1.2599F, 3.0179F), PartPose.ZERO); // Right ear outer body
        head.addOrReplaceChild("panda_part_65", CubeListBuilder.create().texOffs(6, 12).addBox(2.6799F, -10.9548F, -3.1460F, 1.7129F, 0.5241F, 2.4818F), PartPose.ZERO); // Right ear rounded top
        head.addOrReplaceChild("panda_part_66", CubeListBuilder.create().texOffs(134, 12).addBox(2.8081F, -10.4976F, -3.5887F, 1.5030F, 1.2377F, 0.1864F), PartPose.ZERO); // Right ear recessed lining
        head.addOrReplaceChild("panda_part_67", CubeListBuilder.create().texOffs(198, 12).addBox(2.9828F, -10.3638F, -3.6586F, 1.2001F, 0.4014F, 0.1573F), PartPose.ZERO); // Right ear inner bright top
        head.addOrReplaceChild("panda_part_68", CubeListBuilder.create().texOffs(454, 12).addBox(2.5517F, -10.3415F, -0.5826F, 2.0158F, 1.0927F, 0.5593F), PartPose.ZERO); // Right ear back bulge
        head.addOrReplaceChild("panda_part_69", CubeListBuilder.create().texOffs(454, 12).addBox(2.7382F, -11.0885F, -2.8546F, 1.5962F, 0.2119F, 1.9458F), PartPose.ZERO); // Right ear top black edge
        head.addOrReplaceChild("panda_part_70", CubeListBuilder.create().texOffs(134, 12).addBox(-3.3790F, -8.9477F, -2.5051F, 0.5942F, 0.0747F, 0.7107F), PartPose.ZERO); // Crown fabric pixel 1
        head.addOrReplaceChild("panda_part_71", CubeListBuilder.create().texOffs(70, 12).addBox(-1.8061F, -8.9477F, -1.9225F, 0.5943F, 0.0747F, 0.7107F), PartPose.ZERO); // Crown fabric pixel 2
        head.addOrReplaceChild("panda_part_72", CubeListBuilder.create().texOffs(134, 12).addBox(0.2912F, -8.9477F, -1.6312F, 0.5942F, 0.0747F, 0.7107F), PartPose.ZERO); // Crown fabric pixel 3
        head.addOrReplaceChild("panda_part_73", CubeListBuilder.create().texOffs(70, 12).addBox(2.8546F, -8.9477F, -3.0877F, 0.5943F, 0.0747F, 0.7107F), PartPose.ZERO); // Crown fabric pixel 4
        head.addOrReplaceChild("panda_part_74", CubeListBuilder.create().texOffs(134, 12).addBox(-3.4955F, -8.9477F, 1.5147F, 0.5942F, 0.0747F, 0.7107F), PartPose.ZERO); // Crown fabric pixel 5
        head.addOrReplaceChild("panda_part_75", CubeListBuilder.create().texOffs(70, 12).addBox(1.7478F, -8.9477F, 2.1555F, 0.5942F, 0.0747F, 0.7108F), PartPose.ZERO); // Crown fabric pixel 6
        head.addOrReplaceChild("panda_part_76", CubeListBuilder.create().texOffs(390, 12).addBox(-5.0336F, -10.2300F, -3.4489F, 0.3378F, 1.2599F, 3.0179F), PartPose.ZERO); // Left ear outer body
        head.addOrReplaceChild("panda_part_77", CubeListBuilder.create().texOffs(390, 12).addBox(-2.4119F, -10.2300F, -3.4489F, 0.3380F, 1.2599F, 3.0179F), PartPose.ZERO); // Left ear outer body
        head.addOrReplaceChild("panda_part_78", CubeListBuilder.create().texOffs(390, 12).addBox(2.0740F, -10.2300F, -3.4489F, 0.3380F, 0.9812F, 3.0179F), PartPose.ZERO); // Right ear outer body
        head.addOrReplaceChild("panda_part_79", CubeListBuilder.create().texOffs(390, 12).addBox(4.6957F, -10.2300F, -3.4489F, 0.3378F, 0.9812F, 3.0179F), PartPose.ZERO); // Right ear outer body
        return LayerDefinition.create(mesh, 512, 128);
    }
}
