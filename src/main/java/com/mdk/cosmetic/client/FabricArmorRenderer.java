package com.mdk.cosmetic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import com.mdk.cosmetic.platform.CosmeticArmorItem;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

/** Preserves custom hat geometry, dye colors, overlays, and ordinary Collector armor. */
public final class FabricArmorRenderer implements ArmorRenderer {
    private final ClientEvents.ArmorAppearance appearance;
    private net.minecraft.client.renderer.entity.ArmorModelSet<HumanoidModel<net.minecraft.client.renderer.entity.state.HumanoidRenderState>> armorModels;
    private final java.util.Map<net.minecraft.client.model.geom.ModelPart,ArmorSnapshotModel> snapshots=new java.util.IdentityHashMap<>();
    public FabricArmorRenderer(ClientEvents.ArmorAppearance appearance){this.appearance=appearance;}
    @Override @SuppressWarnings({"rawtypes","unchecked"})
    public void render(PoseStack pose,SubmitNodeCollector buffers,ItemStack stack,net.minecraft.client.renderer.entity.state.HumanoidRenderState entity,EquipmentSlot slot,int light,HumanoidModel<net.minecraft.client.renderer.entity.state.HumanoidRenderState> context){
        if(!(stack.getItem() instanceof CosmeticArmorItem armor))return;
        var selected=appearance.getHumanoidArmorModel(entity,stack,slot,context);
        if(selected==context){
            if(armorModels==null)armorModels=net.minecraft.client.renderer.entity.ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR,Minecraft.getInstance().getEntityModels(),part->new HumanoidModel<net.minecraft.client.renderer.entity.state.HumanoidRenderState>(part));
            selected=armorModels.get(slot);
        }
        ((HumanoidModel)selected).setupAnim(entity);
        selected.head.loadPose(context.head.storePose());selected.hat.loadPose(context.hat.storePose());selected.body.loadPose(context.body.storePose());selected.leftArm.loadPose(context.leftArm.storePose());selected.rightArm.loadPose(context.rightArm.storePose());selected.leftLeg.loadPose(context.leftLeg.storePose());selected.rightLeg.loadPose(context.rightLeg.storePose());
        selected.setAllVisible(false);
        switch(slot){
            case HEAD -> {selected.head.visible=true;selected.hat.visible=true;}
            case CHEST -> {selected.body.visible=true;selected.leftArm.visible=true;selected.rightArm.visible=true;}
            case LEGS -> {selected.body.visible=true;selected.leftLeg.visible=true;selected.rightLeg.visible=true;}
            case FEET -> {selected.leftLeg.visible=true;selected.rightLeg.visible=true;}
            default -> {return;}
        }
        var frozen=snapshots.computeIfAbsent(selected.root(),ArmorSnapshotModel::new);var snapshot=frozen.capture();
        var layers=armor.getMaterial().layers();
        for(int i=0;i<layers.size();i++){
            var layer=layers.get(i);
            int fallback=layer.dyeable()?0xFF000000|DyedItemColor.getOrDefault(stack,appearance.getDefaultDyeColor(stack)):0xFFFFFFFF;
            int tint=appearance.getArmorLayerTintColor(stack,entity,layer,i,fallback);
            if((tint>>>24)==0)continue;
            buffers.order(i*2+1).submitModel(frozen,snapshot,pose,RenderType.armorCutoutNoCull(layer.texture(slot==EquipmentSlot.LEGS)),light,OverlayTexture.NO_OVERLAY,tint,null,entity.outlineColor,null);
            if(stack.hasFoil())buffers.order(i*2+2).submitModel(frozen,snapshot,pose,RenderType.armorEntityGlint(),light,OverlayTexture.NO_OVERLAY,tint,null,entity.outlineColor,null);
        }
        var trim=stack.get(net.minecraft.core.component.DataComponents.TRIM);
        if(trim!=null && !stack.is(com.mdk.cosmetic.MDKCosmetic.COLLECTOR_HELMET.get())){
            var texture=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(trim.pattern().value().assetId().getNamespace(),"trims/entity/"+(slot==EquipmentSlot.LEGS?"humanoid_leggings/":"humanoid/")+trim.pattern().value().assetId().getPath()+"_"+trim.material().value().assets().base().suffix());
            var atlas=Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(net.minecraft.data.AtlasIds.ARMOR_TRIMS);
            var sprite=atlas.getSprite(texture);
            buffers.order(layers.size()*2+1).submitModel(frozen,snapshot,pose,net.minecraft.client.renderer.Sheets.armorTrimsSheet(trim.pattern().value().decal()),light,OverlayTexture.NO_OVERLAY,0xFFFFFFFF,sprite,entity.outlineColor,null);
        }
    }
}
