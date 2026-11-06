package com.mdk.cosmetic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
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
    private HumanoidModel<net.minecraft.client.renderer.entity.state.HumanoidRenderState> outer,inner;
    public FabricArmorRenderer(ClientEvents.ArmorAppearance appearance){this.appearance=appearance;}
    @Override @SuppressWarnings({"rawtypes","unchecked"})
    public void render(PoseStack pose,MultiBufferSource buffers,ItemStack stack,net.minecraft.client.renderer.entity.state.HumanoidRenderState entity,EquipmentSlot slot,int light,HumanoidModel<net.minecraft.client.renderer.entity.state.HumanoidRenderState> context){
        if(!(stack.getItem() instanceof CosmeticArmorItem armor))return;
        var selected=appearance.getHumanoidArmorModel(entity,stack,slot,context);
        if(selected==context){
            if(outer==null){var models=Minecraft.getInstance().getEntityModels();outer=new HumanoidModel<>(models.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));inner=new HumanoidModel<>(models.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));}
            selected=slot==EquipmentSlot.LEGS?inner:outer;
        }
        ((HumanoidModel)context).copyPropertiesTo((HumanoidModel)selected);
        selected.setAllVisible(false);
        switch(slot){
            case HEAD -> {selected.head.visible=true;selected.hat.visible=true;}
            case CHEST -> {selected.body.visible=true;selected.leftArm.visible=true;selected.rightArm.visible=true;}
            case LEGS -> {selected.body.visible=true;selected.leftLeg.visible=true;selected.rightLeg.visible=true;}
            case FEET -> {selected.leftLeg.visible=true;selected.rightLeg.visible=true;}
            default -> {return;}
        }
        var layers=armor.getMaterial().layers();
        for(int i=0;i<layers.size();i++){
            var layer=layers.get(i);
            int fallback=layer.dyeable()?0xFF000000|DyedItemColor.getOrDefault(stack,appearance.getDefaultDyeColor(stack)):0xFFFFFFFF;
            int tint=appearance.getArmorLayerTintColor(stack,entity,layer,i,fallback);
            if((tint>>>24)==0)continue;
            var consumer=ItemRenderer.getArmorFoilBuffer(buffers,RenderType.armorCutoutNoCull(layer.texture(slot==EquipmentSlot.LEGS)),stack.hasFoil());
            selected.renderToBuffer(pose,consumer,light,OverlayTexture.NO_OVERLAY,tint);
        }
        var trim=stack.get(net.minecraft.core.component.DataComponents.TRIM);
        if(trim!=null && !stack.is(com.mdk.cosmetic.MDKCosmetic.COLLECTOR_HELMET.get())){
            var texture=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(trim.pattern().value().assetId().getNamespace(),"trims/entity/"+(slot==EquipmentSlot.LEGS?"humanoid_leggings/":"humanoid/")+trim.pattern().value().assetId().getPath()+"_"+trim.material().value().assets().base().suffix());
            var atlas=Minecraft.getInstance().getModelManager().getAtlas(net.minecraft.client.renderer.Sheets.ARMOR_TRIMS_SHEET);
            var sprite=atlas.getSprite(texture);
            var consumer=sprite.wrap(buffers.getBuffer(net.minecraft.client.renderer.Sheets.armorTrimsSheet(trim.pattern().value().decal())));
            selected.renderToBuffer(pose,consumer,light,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);
        }
    }
}
