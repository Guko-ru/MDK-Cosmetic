package com.mdk.cosmetic;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** One-time Productivity sphere, 15 minute golden apple + Haste III and Speed II. */
public final class ProductivityOrbItem extends Item {
    public static final int DURATION=20*60*15;
    public ProductivityOrbItem(Properties props){super(props);}
    @Override public void appendHoverText(ItemStack stack, Item.TooltipContext context,
            net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack,context,display,tooltip,flag);
        tooltip.accept(net.minecraft.network.chat.Component.translatable("tooltip.mdkcosmetic.eat_me")
                .withStyle(net.minecraft.ChatFormatting.RED));
    }
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack){return ItemUseAnimation.EAT;}
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity){return 32;}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity consumer){
        if(!level.isClientSide()) {
            consumer.addEffect(new MobEffectInstance(MobEffects.REGENERATION,DURATION,1));
            consumer.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,DURATION,0));
            consumer.addEffect(new MobEffectInstance(MobEffects.SPEED,DURATION,1));
            consumer.addEffect(new MobEffectInstance(MobEffects.HASTE,DURATION,2));
            consumer.addEffect(new MobEffectInstance(MDKCosmetic.PRODUCTIVITY_AURA.holder(),DURATION,0,false,false,true));
            level.playSound(null,consumer.blockPosition(),SoundEvents.AMETHYST_BLOCK_CHIME,SoundSource.PLAYERS,0.9F,1.6F);
            if(!(consumer instanceof Player player) || !player.getAbilities().instabuild)stack.shrink(1);
        }
        return stack;
    }
}
