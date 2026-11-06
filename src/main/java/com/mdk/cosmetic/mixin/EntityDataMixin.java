package com.mdk.cosmetic.mixin;
import com.mdk.cosmetic.platform.Events;
import com.mdk.cosmetic.platform.PersistentData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Entity.class)
public abstract class EntityDataMixin implements PersistentData {
 @Unique private CompoundTag mdk$persistentData=new CompoundTag();
 public CompoundTag mdk$getPersistentData(){return mdk$persistentData;}
 @Inject(method="saveWithoutId",at=@At("RETURN")) private void mdk$save(CompoundTag tag,CallbackInfoReturnable<CompoundTag> ci){if(!mdk$persistentData.isEmpty())tag.put("MDKCosmeticData",mdk$persistentData.copy());}
 @Inject(method="load",at=@At("TAIL")) private void mdk$load(CompoundTag tag,CallbackInfo ci){mdk$persistentData=tag.contains("MDKCosmeticData",10)?tag.getCompound("MDKCosmeticData").copy():tag.getCompound("NeoForgeData").copy();}
 @Inject(method="isInvulnerableTo",at=@At("RETURN"),cancellable=true) private void mdk$protect(DamageSource source,CallbackInfoReturnable<Boolean> ci){var event=new Events.EntityInvulnerabilityCheckEvent((Entity)(Object)this,source,ci.getReturnValue());Events.dispatch(event);ci.setReturnValue(event.isInvulnerable());}
}
