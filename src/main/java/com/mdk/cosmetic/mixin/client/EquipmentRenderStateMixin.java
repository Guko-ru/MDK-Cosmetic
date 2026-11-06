package com.mdk.cosmetic.mixin.client;
import com.mdk.cosmetic.client.EquipmentRenderData;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(LivingEntityRenderState.class) public abstract class EquipmentRenderStateMixin implements EquipmentRenderData {
 @Unique private ItemStack mdk$helmet=ItemStack.EMPTY;
 @Unique private ItemStack mdk$backpack=ItemStack.EMPTY;
 @Unique private int mdk$entityId;
 public ItemStack mdk$getHelmet(){return mdk$helmet;}
 public ItemStack mdk$getBackpack(){return mdk$backpack;}
 public int mdk$getEntityId(){return mdk$entityId;}
 public void mdk$setEquipment(ItemStack helmet,ItemStack backpack,int entityId){mdk$helmet=helmet;mdk$backpack=backpack;mdk$entityId=entityId;}
}
