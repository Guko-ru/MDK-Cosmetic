package com.mdk.cosmetic.mixin;
import com.mdk.cosmetic.platform.Events;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemEntity.class) public abstract class ItemPickupMixin implements com.mdk.cosmetic.platform.PickupTarget {
 @org.spongepowered.asm.mixin.gen.Accessor("target") public abstract java.util.UUID mdk$getPickupTarget();
 @Unique private ItemStack mdk$beforePickup=ItemStack.EMPTY;
 @Inject(method="playerTouch",at=@At("HEAD")) private void mdk$before(Player player,CallbackInfo ci){mdk$beforePickup=((ItemEntity)(Object)this).getItem().copy();}
 @Inject(method="playerTouch",at=@At("RETURN")) private void mdk$after(Player player,CallbackInfo ci){if(!player.level().isClientSide())Events.dispatch(new Events.ItemEntityPickupEvent.Post(player,mdk$beforePickup,((ItemEntity)(Object)this).getItem()));}
}
