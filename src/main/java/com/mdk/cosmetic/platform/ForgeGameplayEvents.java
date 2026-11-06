package com.mdk.cosmetic.platform;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;
public final class ForgeGameplayEvents {
 public static final class EntityInvulnerabilityCheckEvent extends Event {
  private final Entity entity;private final DamageSource source;private boolean invulnerable;
  public EntityInvulnerabilityCheckEvent(Entity entity,DamageSource source,boolean original){this.entity=entity;this.source=source;invulnerable=original;}
  public Entity getEntity(){return entity;} public DamageSource getSource(){return source;}
  public void setInvulnerable(boolean value){invulnerable=value;} public boolean isInvulnerable(){return invulnerable;}
 }
 public static final class BlockDropsEvent extends Event {
  private final BlockState state;private final Entity breaker;private final ItemStack tool;private final BlockPos pos;private final List<ItemEntity> drops;
  public BlockDropsEvent(BlockState state,Entity breaker,ItemStack tool,BlockPos pos,List<ItemEntity> drops){this.state=state;this.breaker=breaker;this.tool=tool;this.pos=pos;this.drops=drops;}
  public BlockState getState(){return state;} public Entity getBreaker(){return breaker;} public ItemStack getTool(){return tool;} public BlockPos getPos(){return pos;} public List<ItemEntity> getDrops(){return drops;}
 }
 public static final class ItemAttributeModifierEvent extends Event {
  private final ItemStack stack;private final List<ItemAttributeModifiers.Entry> entries;
  public ItemAttributeModifierEvent(ItemStack stack,ItemAttributeModifiers original){this.stack=stack;entries=new ArrayList<>(original.modifiers());}
  public ItemStack getItemStack(){return stack;}
  public void addModifier(Holder<Attribute> attribute,AttributeModifier modifier,EquipmentSlotGroup slot){entries.add(new ItemAttributeModifiers.Entry(attribute,modifier,slot));}
  public ItemAttributeModifiers result(boolean show){return new ItemAttributeModifiers(List.copyOf(entries));}
 }
}
