package com.mdk.cosmetic.platform;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.*;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.RegistryObject;
/** Preserves typed vanilla Holder access while Forge owns deferred registration. */
public final class RegistryEntry<R,T extends R> implements Holder<R> {
 private final ResourceKey<? extends Registry<R>> registryKey;
 private final RegistryObject<T> entry;
 RegistryEntry(ResourceKey<? extends Registry<R>> key,RegistryObject<T> entry){registryKey=key;this.entry=entry;}
 public T get(){return entry.get();}
 public R value(){return get();}
 public ResourceLocation getId(){return entry.getId();}
 public ResourceKey<R> getKey(){return ResourceKey.create(registryKey,getId());}
 @SuppressWarnings("unchecked") public Holder.Reference<R> holder(){return ((Registry<R>)BuiltInRegistries.REGISTRY.get(registryKey.location())).getHolderOrThrow(getKey());}
 public boolean isBound(){return entry.isPresent();}
 public boolean is(ResourceLocation id){return getId().equals(id);}
 public boolean is(ResourceKey<R> key){return getKey().equals(key);}
 public boolean is(Predicate<ResourceKey<R>> predicate){return predicate.test(getKey());}
 public boolean is(TagKey<R> tag){return holder().is(tag);}
 public boolean is(Holder<R> other){return holder().is(other);}
 public Stream<TagKey<R>> tags(){return holder().tags();}
 public Either<ResourceKey<R>,R> unwrap(){return Either.left(getKey());}
 public Optional<ResourceKey<R>> unwrapKey(){return Optional.of(getKey());}
 public Kind kind(){return Kind.REFERENCE;}
 public boolean canSerializeIn(HolderOwner<R> owner){return holder().canSerializeIn(owner);}
}
