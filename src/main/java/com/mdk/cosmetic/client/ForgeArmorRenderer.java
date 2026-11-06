package com.mdk.cosmetic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import com.mdk.cosmetic.platform.CosmeticArmorItem;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

/** Preserves custom hat geometry, dye colors, overlays, and ordinary Collector armor. */
public final class ForgeArmorRenderer {
    private final ArmorAppearance appearance;
    private net.minecraft.client.renderer.entity.ArmorModelSet<HumanoidModel<net.minecraft.client.renderer.entity.state.HumanoidRenderState>> armorModels;
    private final java.util.Map<net.minecraft.client.model.geom.ModelPart,ArmorSnapshotModel> snapshots=new java.util.IdentityHashMap<>();
    public ForgeArmorRenderer(ArmorAppearance appearance){this.appearance=appearance;}
    @SuppressWarnings({"rawtypes","unchecked"})
    public void render(PoseStack pose,SubmitNodeCollector buffers,ItemStack stack,net.minecraft.client.renderer.entity.state.HumanoidRenderState entity,EquipmentSlot slot,int light,HumanoidModel<net.minecraft.client.renderer.entity.state.HumanoidRenderState> context){
        if(!(stack.getItem() instanceof CosmeticArmorItem armor))return;
        var selected=appearance.getHumanoidArmorModel(entity,stack,slot,context);
        if(selected==context){
            if(armorModels==null)armorModels=net.minecraft.client.renderer.entity.ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR,Minecraft.getInstance().getEntityModels(),part->new HumanoidModel<net.minecraft.client.renderer.entity.state.HumanoidRenderState>(part));
            selected=armorModels.get(slot);
        }
        ((HumanoidModel)selected).setupAnim(entity);
        selected.head.loadPose(context.head.storePose());selected.hat.loadPose(context.hat.storePose());selected.body.loadPose(context.body.storePose());selected.leftArm.loadPose(context.leftArm.storePose());selected.rightArm.loadPose(context.rightArm.storePose());selected.leftLeg.loadPose(context.leftLeg.storePose());selected.rightLeg.loadPose(context.rightLeg.storePose());
        com.mdk.cosmetic.client.ModelVisibility.set(selected,false);
        switch(slot){
            case HEAD -> {selected.head.visible=true;selected.hat.visible=true;}
            case CHEST -> {selected.body.visible=true;selected.leftArm.visible=true;selected.rightArm.visible=true;}
            case LEGS -> {selected.body.visible=true;selected.leftLeg.visible=true;selected.rightLeg.visible=true;}
            case FEET -> {selected.leftLeg.visible=true;selected.rightLeg.visible=true;}
            default -> {return;}
        }
        var frozen=snapshots.computeIfAbsent(selected.root(),ArmorSnapshotModel::new);var snapshot=frozen.capture();
        var layers=armor.getMaterial().layers();boolean foil=stack.hasFoil();
        for(int i=0;i<layers.size();i++){
            var layer=layers.get(i);
            int fallback=layer.dyeable()?0xFF000000|DyedItemColor.getOrDefault(stack,appearance.getDefaultDyeColor(stack)):0xFFFFFFFF;
            int tint=appearance.getArmorLayerTintColor(stack,entity,layer,i,fallback);
            if((tint>>>24)==0)continue;
            buffers.order(i*2+1).submitModel(frozen,snapshot,pose,(foil?RenderTypes.armorCutoutNoCullGlint(layer.texture(slot==EquipmentSlot.LEGS)):RenderTypes.armorCutoutNoCull(layer.texture(slot==EquipmentSlot.LEGS))),light,OverlayTexture.NO_OVERLAY,tint,null,entity.outlineColor);
            foil=false;
        }
        var trim=stack.get(net.minecraft.core.component.DataComponents.TRIM);
        if(trim!=null && !stack.is(com.mdk.cosmetic.MDKCosmetic.COLLECTOR_HELMET.get())){
            var client=Minecraft.getInstance();
            var type=slot==EquipmentSlot.LEGS?net.minecraft.client.resources.model.EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS:net.minecraft.client.resources.model.EquipmentClientInfo.LayerType.HUMANOID;
            var texture=ForgeArmorTrimTextures.resolve(client,armor.getMaterial().nativeMaterial().assetId(),trim,type);
            buffers.order(layers.size()*2+1).submitModel(frozen,snapshot,pose,RenderTypes.armorTrim(texture.textureLocation(),trim.pattern().value().decal()),light,OverlayTexture.NO_OVERLAY,0xFFFFFFFF,texture,entity.outlineColor);
            if(stack.hasFoil())buffers.order(layers.size()*2+2).submitModel(frozen,snapshot,pose,RenderTypes.trimmedArmorGlint(),light,OverlayTexture.NO_OVERLAY,0xFFFFFFFF,null,0);
        }
    }
}
