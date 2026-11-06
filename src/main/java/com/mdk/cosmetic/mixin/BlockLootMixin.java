package com.mdk.cosmetic.mixin;
import java.util.*;
import com.mdk.cosmetic.platform.ForgeGameplayEvents;
import net.minecraftforge.common.MinecraftForge;
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
 @Inject(method="getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",at=@At("RETURN"),cancellable=true)
 private static void mdk$loot(BlockState state,ServerLevel level,BlockPos pos,BlockEntity blockEntity,Entity breaker,ItemStack tool,CallbackInfoReturnable<List<ItemStack>> ci){var drops=new ArrayList<ItemEntity>();for(var stack:ci.getReturnValue())drops.add(new ItemEntity(level,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,stack));MinecraftForge.EVENT_BUS.post(new ForgeGameplayEvents.BlockDropsEvent(state,breaker,tool,pos,drops));ci.setReturnValue(drops.stream().map(ItemEntity::getItem).toList());}
}
