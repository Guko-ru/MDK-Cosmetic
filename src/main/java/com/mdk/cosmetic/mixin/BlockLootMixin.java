package com.mdk.cosmetic.mixin;
import java.util.*;
import com.mdk.cosmetic.platform.Events;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Block.class) public abstract class BlockLootMixin {
 @Inject(method="getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemInstance;)Ljava/util/List;",at=@At("RETURN"),cancellable=true)
 private static void mdk$loot(BlockState state,ServerLevel level,BlockPos pos,BlockEntity blockEntity,Entity breaker,net.minecraft.world.item.ItemInstance toolInstance,CallbackInfoReturnable<List<ItemStack>> ci){ItemStack tool=toolInstance instanceof ItemStack stack?stack:toolInstance instanceof net.minecraft.world.item.ItemStackTemplate template?template.create():new ItemStack(toolInstance.typeHolder(),toolInstance.count());var drops=new ArrayList<ItemEntity>();for(var stack:ci.getReturnValue())drops.add(new ItemEntity(level,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,stack));Events.dispatch(new Events.BlockDropsEvent(state,breaker,tool,pos,drops));ci.setReturnValue(drops.stream().map(ItemEntity::getItem).toList());}
}
