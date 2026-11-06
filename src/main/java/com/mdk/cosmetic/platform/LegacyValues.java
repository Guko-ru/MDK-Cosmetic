package com.mdk.cosmetic.platform;
import com.mojang.serialization.*;
import net.minecraft.nbt.*;
import net.minecraft.world.level.storage.*;
import java.util.stream.Stream;
/** Uses native ValueInput/Output codecs while retaining every existing custom NBT field. */
public final class LegacyValues {
 public static final MapCodec<CompoundTag> ROOT=new MapCodec<>() {
  @Override public <T> Stream<T> keys(DynamicOps<T> ops){return Stream.empty();}
  @Override public <T> DataResult<CompoundTag> decode(DynamicOps<T> ops,MapLike<T> input){var tag=new CompoundTag();input.entries().forEach(pair->tag.put(ops.getStringValue(pair.getFirst()).getOrThrow(),ops.convertTo(NbtOps.INSTANCE,pair.getSecond())));return DataResult.success(tag);}
  @Override public <T> RecordBuilder<T> encode(CompoundTag tag,DynamicOps<T> ops,RecordBuilder<T> prefix){for(var entry:tag.entrySet())prefix=prefix.add(entry.getKey(),NbtOps.INSTANCE.convertTo(ops,entry.getValue()));return prefix;}
 };
 public static CompoundTag read(ValueInput input){return input.read(ROOT).orElseGet(CompoundTag::new);}
 public static void write(ValueOutput output,CompoundTag tag){output.store(ROOT,tag);}
 public static CompoundTag saveEntity(net.minecraft.world.entity.Entity entity){var output=TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,entity.registryAccess());entity.saveWithoutId(output);return output.buildResult();}
 public static void loadEntity(net.minecraft.world.entity.Entity entity,CompoundTag tag){entity.load(TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,entity.registryAccess(),tag));}
}
