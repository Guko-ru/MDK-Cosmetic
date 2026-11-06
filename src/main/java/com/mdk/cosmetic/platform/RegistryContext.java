package com.mdk.cosmetic.platform;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
/** Gives factory-created properties the exact key of the pending mod registration. */
public final class RegistryContext implements AutoCloseable {
 private record Pending(Registry<?> registry,ResourceLocation id) {}
 private static final ThreadLocal<Pending> CURRENT=new ThreadLocal<>();
 private final Pending previous;
 private RegistryContext(Registry<?> registry,ResourceLocation id){previous=CURRENT.get();CURRENT.set(new Pending(registry,id));}
 public static RegistryContext enter(Registry<?> registry,ResourceLocation id){return new RegistryContext(registry,id);}
 @Override public void close(){if(previous==null)CURRENT.remove();else CURRENT.set(previous);}
 public static void applyItem(Item.Properties properties){var p=CURRENT.get();if(p!=null&&p.registry()==BuiltInRegistries.ITEM){properties.setId(ResourceKey.create(Registries.ITEM,p.id()));if(BuiltInRegistries.BLOCK.containsKey(p.id()))properties.useBlockDescriptionPrefix();}}
 public static void applyBlock(BlockBehaviour.Properties properties){var p=CURRENT.get();if(p!=null&&p.registry()==BuiltInRegistries.BLOCK)properties.setId(ResourceKey.create(Registries.BLOCK,p.id()));}
 public static ResourceKey<EntityType<?>> entityKey(){var p=CURRENT.get();if(p==null||p.registry()!=BuiltInRegistries.ENTITY_TYPE)throw new IllegalStateException("Entity factory must run in registry context");return ResourceKey.create(Registries.ENTITY_TYPE,p.id());}
}
