package com.mdk.cosmetic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/** Humane wool collection from llamas, including trader llamas. */
public final class LlamaFurShearing {
    private static final String NEXT = "mdkcosmetic.nextLlamaFurShearing";
    private static final long COOLDOWN_MS = 5L * 60L * 1000L;
    private LlamaFurShearing() { }

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Llama llama)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack shears = event.getEntity().getMainHandItem();
        if (!shears.is(Items.SHEARS)) return;
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (llama.level().isClientSide()) return;
        CompoundTag data = com.mdk.cosmetic.platform.PersistentData.of(llama);
        long now = System.currentTimeMillis();
        long next = data.getLong(NEXT);
        if (next > now && next - now <= COOLDOWN_MS) {
            event.getEntity().displayClientMessage(Component.translatable(
                    "message.mdkcosmetic.llama_regrowing", (next - now + 59999L) / 60000L), true);
            return;
        }
        data.putLong(NEXT, now + COOLDOWN_MS);
        com.mdk.cosmetic.platform.EntityDrops.spawn(llama,new ItemStack(MDKCosmetic.LLAMA_FUR.get()));
        llama.playSound(SoundEvents.SHEEP_SHEAR, 0.70F, 1.12F);
        if (!event.getEntity().getAbilities().instabuild) {
            shears.hurtAndBreak(1, event.getEntity(), EquipmentSlot.MAINHAND);
        }
    }

    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Llama llama) || llama.level().isClientSide()) return;
        if (llama.getRandom().nextFloat() < 0.10F) {
            event.getDrops().add(new ItemEntity(llama.level(), llama.getX(), llama.getY() + 0.15D,
                    llama.getZ(), new ItemStack(MDKCosmetic.LLAMA_FUR.get())));
        }
    }
}
