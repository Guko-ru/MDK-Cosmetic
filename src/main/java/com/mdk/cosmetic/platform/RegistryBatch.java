package com.mdk.cosmetic.platform;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
public final class RegistryBatch<R> {
    private final Registry<R> registry;
    private final String namespace;
    private final List<RegistryEntry<R,?>> entries=new ArrayList<>();
    private RegistryBatch(Registry<R> registry,String namespace){this.registry=registry;this.namespace=namespace;}
    @SuppressWarnings("unchecked") public static <R> RegistryBatch<R> create(ResourceKey<? extends Registry<R>> key,String namespace){return new RegistryBatch<>((Registry<R>)BuiltInRegistries.REGISTRY.get(key.location()),namespace);}
    public <T extends R> RegistryEntry<R,T> register(String name,Supplier<T> factory){var entry=new RegistryEntry<R,T>(registry,ResourceLocation.fromNamespaceAndPath(namespace,name),factory);entries.add(entry);return entry;}
    public void registerAll(){entries.forEach(RegistryEntry::register);}
}
