package com.mdk.cosmetic.client;
import com.mdk.cosmetic.mixin.client.EntityEquipmentAssetsAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.resources.palette.PalettedTextureManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
/** Resolves trim palettes and equipment overrides without relying on vanilla's private record. */
public final class ForgeArmorTrimTextures {
 private ForgeArmorTrimTextures(){}
 public static PalettedTextureManager.Handle resolve(Minecraft client,ResourceKey<EquipmentAsset> asset,ArmorTrim trim,EquipmentClientInfo.LayerType layer){
  var info=((EntityEquipmentAssetsAccessor)client.getEntityRenderDispatcher()).mdk$equipmentAssets().get(asset);
  var pattern=trim.pattern().value().assetId();var palette=trim.material().value().paletteId();
  for(var override:info.trimOverrides())if(override.predicate().matches(trim)){
   pattern=override.textureId().orElse(pattern);palette=override.paletteId().orElse(null);break;
  }
  var base=pattern.withPath(path->layer.trimAssetPrefix()+"/"+path);
  if(palette!=null)return client.getPalettedTextureManager().getOrPrepare(base,palette);
  final Identifier texture=base.withPath(path->"textures/"+path+".png");
  return new PalettedTextureManager.Handle(){
   public Identifier textureLocation(){return texture;}
   public float getU(float u){return u;}
   public float getV(float v){return v;}
  };
 }
}
