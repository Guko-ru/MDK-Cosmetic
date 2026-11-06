package com.mdk.cosmetic.platform;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
public final class RegistryEntry<R,T extends R> implements Supplier<T> {
    private final Registry<R> registry;
    private final ResourceLocation id;
    private final Supplier<T> factory;
    private T value;
    RegistryEntry(Registry<R> registry,ResourceLocation id,Supplier<T> factory){this.registry=registry;this.id=id;this.factory=factory;}
    void register(){if(registry!=null)throw new IllegalStateException("Registry insertion requires Forge RegisterEvent");if(value==null)try(var context=RegistryContext.enter(null,id)){value=factory.get();}}
    void register(net.minecraftforge.registries.RegisterEvent event){if(value!=null)return;event.register(registry.key(),id,()->{try(var context=RegistryContext.enter(registry,id)){value=factory.get();return value;}});}
    @Override public T get(){ if(value==null) throw new IllegalStateException("Registry entry used before registration: "+id);return value; }
    public Holder.Reference<R> holder(){ return registry.getOrThrow(ResourceKey.create(registry.key(),id)); }
    public ResourceLocation getId(){ return id; }
    public ResourceKey<R> getKey(){ return ResourceKey.create(registry.key(),id); }
}
