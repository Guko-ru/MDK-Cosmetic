package com.mdk.cosmetic.client;

import com.mdk.cosmetic.CollectorSoulEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Original low-poly spectral polyhedron, with rotating violet halo and drifting shards. */
public final class CollectorSoulModel extends EntityModel<net.minecraft.client.renderer.entity.state.LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("mdkcosmetic","collector_soul"),"main");
    private final ModelPart root,core,ring,orbit;
    public CollectorSoulModel(ModelPart root) {
        super(root,RenderTypes::entityTranslucentEmissive);
        this.root=root;
        this.core=root.getChild("core");
        this.ring=root.getChild("ring");
        this.orbit=root.getChild("orbit");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh=new MeshDefinition();
        PartDefinition root=mesh.getRoot();
        PartDefinition core=root.addOrReplaceChild("core",CubeListBuilder.create()
                .texOffs(0,0).addBox(-3,-3,-3,6,6,6)
                .texOffs(24,0).addBox(-2.5F,-4,-2.5F,5,8,5)
                .texOffs(0,14).addBox(-4,-2,-2,8,4,4),PartPose.ZERO);
        PartDefinition ring=root.addOrReplaceChild("ring",CubeListBuilder.create(),PartPose.ZERO);
        // Segmented floating orbit, no perfectly spherical silhouette copied from other media.
        for(int i=0;i<12;i++) {
            float theta=(float)(2*Math.PI*i/12.0);
            float x=(float)Math.cos(theta)*6.2F;
            float z=(float)Math.sin(theta)*6.2F;
            ring.addOrReplaceChild("arc"+i,CubeListBuilder.create()
                    .texOffs(0,26).addBox(-1.2F,-0.45F,-0.65F,2.4F,0.9F,1.3F),
                    PartPose.offsetAndRotation(x,0,z,0,-theta,0));
        }
        PartDefinition orbit=root.addOrReplaceChild("orbit",CubeListBuilder.create(),PartPose.ZERO);
        for(int i=0;i<8;i++) {
            float a=(float)(i*2*Math.PI/8.0);
            float x=(float)Math.cos(a)*8.0F;
            float z=(float)Math.sin(a)*8.0F;
            float y=(float)(Math.sin(a*2)*3.5);
            orbit.addOrReplaceChild("crystal"+i,CubeListBuilder.create()
                    .texOffs(32,24).addBox(-0.9F,-1.4F,-0.8F,1.8F,2.8F,1.6F),
                    PartPose.offsetAndRotation(x,y,z,a*0.3F,a,0.35F));
        }
        return LayerDefinition.create(mesh,64,64);
    }
    @Override public void setupAnim(net.minecraft.client.renderer.entity.state.LivingEntityRenderState state) {
        super.setupAnim(state);
        float t=state.ageInTicks;
        core.xRot=(float)Math.sin(t*0.032F)*0.12F;
        core.yRot=t*0.009F;
        core.zRot=(float)Math.cos(t*0.023F)*0.10F;
        ring.xRot=0.4F+(float)Math.sin(t*0.025F)*0.18F;
        ring.yRot=t*0.013F;
        ring.zRot=0.35F;
        orbit.xRot=0.5F;
        orbit.yRot=-t*0.018F;
        orbit.zRot=(float)Math.cos(t*0.017F)*0.13F;
    }
}
