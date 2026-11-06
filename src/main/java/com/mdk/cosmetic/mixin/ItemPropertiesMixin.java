package com.mdk.cosmetic.mixin;
import com.mdk.cosmetic.platform.*;
import net.minecraft.world.item.Item;
import net.minecraft.core.component.DataComponentMap;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Item.Properties.class) public abstract class ItemPropertiesMixin implements ItemPropertiesAccess {
 @Accessor("components") public abstract DataComponentMap.Builder mdk$getComponents();
 @Inject(method="<init>",at=@At("RETURN")) private void mdk$id(CallbackInfo ci){RegistryContext.applyItem((Item.Properties)(Object)this);}
}
