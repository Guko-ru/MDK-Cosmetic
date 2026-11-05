package com.mdk.cosmetic;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** One-use Collector's Greed Sphere. Effects last five minutes (6000 ticks). */
public final class GreedOrbItem extends Item {
    public static final int DURATION = 20 * 60 * 5;
    public GreedOrbItem(Properties properties) { super(properties); }
    @Override public void appendHoverText(ItemStack stack, Item.TooltipContext context,
            java.util.List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack,context,tooltip,flag);
        tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.mdkcosmetic.eat_me")
                .withStyle(net.minecraft.ChatFormatting.RED));
    }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.EAT; }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 32; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        if (!level.isClientSide()) {
            consumer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, DURATION, 1));
            consumer.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, DURATION, 1));
            consumer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, DURATION, 1));
            consumer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DURATION, 1));
            consumer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, DURATION, 1));
            consumer.addEffect(new MobEffectInstance(MDKCosmetic.GREED_AURA, DURATION, 0, false, false, true));
            level.playSound(null,consumer.blockPosition(),SoundEvents.AMETHYST_BLOCK_CHIME,SoundSource.PLAYERS,1F,0.75F);
            if (!(consumer instanceof Player player) || !player.getAbilities().instabuild) stack.shrink(1);
        }
        return stack;
    }
}
