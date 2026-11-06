package com.mdk.cosmetic.mixin;
import com.mdk.cosmetic.platform.*;
import net.minecraft.world.item.Item;
import net.minecraft.core.component.DataComponentMap;
import org.spongepowered.asm.mixin.*;
import net.minecraft.core.component.DataComponentType;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Item.Properties.class) public abstract class ItemPropertiesMixin implements ItemPropertiesAccess {
 // Native properties now defer component construction. Keep the explicitly supplied
 // values so cosmetic armor can restore its configured durability after humanoidArmor.
 @Unique private final DataComponentMap.Builder mdk$explicitComponents=DataComponentMap.builder();
 public DataComponentMap.Builder mdk$getComponents(){return mdk$explicitComponents;}
 @Inject(method="component",at=@At("HEAD"))
 private <T> void mdk$component(DataComponentType<T> type,T value,CallbackInfoReturnable<Item.Properties> ci){mdk$explicitComponents.set(type,value);}
 @Inject(method="<init>",at=@At("RETURN")) private void mdk$id(CallbackInfo ci){RegistryContext.applyItem((Item.Properties)(Object)this);}
}
