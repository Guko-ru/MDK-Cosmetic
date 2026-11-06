package com.mdk.cosmetic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/** Non-harmful modded material: a shed skin fragment, not an actual axolotl scale. */
public final class AxolotlSkinHarvest {
    private static final String NEXT = "mdkcosmetic.nextAxolotlShed";
    private static final long COOLDOWN = 5L * 60L * 1000L;
    private AxolotlSkinHarvest() { }

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Axolotl axolotl)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack shears = event.getEntity().getMainHandItem();
        if (!shears.is(Items.SHEARS)) return;
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (axolotl.level().isClientSide()) return;
        CompoundTag data = axolotl.getPersistentData();
        long now = System.currentTimeMillis();
        long next = data.getLong(NEXT);
        if (next > now && next - now <= COOLDOWN) {
            long minutes = (next-now+59999L)/60000L;
            event.getEntity().displayClientMessage(
                Component.translatable("message.mdkcosmetic.axolotl_regrowing", minutes), true);
            return;
        }
        data.putLong(NEXT,now+COOLDOWN);
        axolotl.spawnAtLocation(new ItemStack(MDKCosmetic.AXOLOTL_SHED.get()));
        axolotl.playSound(SoundEvents.SHEEP_SHEAR,0.65F,1.50F);
        if (!event.getEntity().getAbilities().instabuild)
            shears.hurtAndBreak(1,event.getEntity(),EquipmentSlot.MAINHAND);
    }

    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Axolotl axolotl) || axolotl.level().isClientSide()) return;
        if (axolotl.getRandom().nextFloat() < 0.10F)
            event.getDrops().add(new ItemEntity(axolotl.level(), axolotl.getX(),
                    axolotl.getY()+0.15D,axolotl.getZ(),
                    new ItemStack(MDKCosmetic.AXOLOTL_SHED.get())));
    }
}
