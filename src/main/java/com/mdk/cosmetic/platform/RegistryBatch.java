package com.mdk.cosmetic.platform;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
public final class RegistryBatch<R> {
 private final DeferredRegister<R> delegate;
 private final ResourceKey<? extends Registry<R>> key;
 private RegistryBatch(ResourceKey<? extends Registry<R>> key,String namespace){this.key=key;delegate=DeferredRegister.create(key,namespace);}
 public static <R> RegistryBatch<R> create(ResourceKey<? extends Registry<R>> key,String namespace){return new RegistryBatch<>(key,namespace);}
 public <T extends R> RegistryEntry<R,T> register(String id,Supplier<T> factory){return new RegistryEntry<>(key,delegate.register(id,factory));}
 public void register(IEventBus bus){delegate.register(bus);}
}
