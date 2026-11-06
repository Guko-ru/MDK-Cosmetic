package com.mdk.cosmetic.platform;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.ArmorMaterial;
/** Original material statistics and texture layers backed by the new native equipment component. */
public final class ArmorDefinition {
 private final ArmorMaterial material; private final boolean enchantable;
 private final List<Layer> layers;
 public ArmorDefinition(Map<ArmorType,Integer> defense,int enchantment,Holder<SoundEvent> sound,TagKey<Item> repairs,List<Layer> layers,float toughness,float knockback){this.layers=List.copyOf(layers);this.enchantable=enchantment>0;material=new ArmorMaterial(1,Map.copyOf(defense),Math.max(1,enchantment),sound,toughness,knockback,repairs,net.minecraft.resources.ResourceKey.create(net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID,layers.getFirst().id()));}
 public boolean enchantable(){return enchantable;} public ArmorMaterial nativeMaterial(){return material;}
 public List<Layer> layers(){return layers;}
 public record Layer(Identifier id,String suffix,boolean dyeable) {
  public Layer(Identifier id){this(id,"",false);}
  public Identifier texture(boolean inner){return Identifier.fromNamespaceAndPath(id.getNamespace(),"textures/models/armor/"+id.getPath()+"_layer_"+(inner?2:1)+suffix+".png");}
 }
}
