package com.mdk.cosmetic;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** The inventory is stored on the item. A bound item contains only the remote memory identity. */
public final class CollectorBackpackItem extends Item {
    public CollectorBackpackItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(stack);
        if (level.isClientSide()) return InteractionResultHolder.sidedSuccess(stack, true);
        if (!(player instanceof ServerPlayer server)) return InteractionResultHolder.fail(stack);
        if (CollectorBackpackData.linked(stack) && CollectorBackpackData.remoteCore(stack, server.getServer()) == null) {
            server.sendSystemMessage(Component.translatable("message.mdkcosmetic.backpack_memory_unavailable"));
            return InteractionResultHolder.fail(stack);
        }
        server.openMenu(new SimpleMenuProvider((id, inventory, p) -> new CollectorBackpackMenu(id, inventory),
                Component.translatable(CollectorBackpackData.linked(stack)
                        ? "menu.mdkcosmetic.collector_memory_terminal"
                        : "menu.mdkcosmetic.collector_backpack")));
        return InteractionResultHolder.success(stack);
    }

    @Override public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null || ctx.getHand() != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        ItemStack backpack = ctx.getItemInHand();
        ItemStack key = player.getOffhandItem();
        if (!key.is(MDKCosmetic.COLLECTOR_BACKPACK_LINK_KEY.get())
                || !ctx.getLevel().getBlockState(ctx.getClickedPos()).is(MDKCosmetic.COLLECTOR_MEMORY_CORE.get()))
            return InteractionResult.PASS;
        if (!(ctx.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        if (!backpack.is(MDKCosmetic.COLLECTOR_BACKPACK_REINFORCED.get())) {
            player.displayClientMessage(Component.translatable("message.mdkcosmetic.backpack_reinforced_required"), true);
            return InteractionResult.FAIL;
        }
        if (CollectorBackpackData.linked(backpack)) return InteractionResult.FAIL;
        if (!(level.getBlockEntity(ctx.getClickedPos()) instanceof CollectorMemoryCoreBlockEntity core)
                || !CollectorMemoryStructure.ready(level, ctx.getClickedPos().above(2))) {
            player.displayClientMessage(Component.translatable("message.mdkcosmetic.backpack_memory_not_ready"), true);
            return InteractionResult.FAIL;
        }
        List<ItemStack> stored = CollectorBackpackData.items(backpack, level.registryAccess());
        if (!core.depositBatch(stored)) {
            player.displayClientMessage(Component.translatable("message.mdkcosmetic.backpack_memory_full"), true);
            return InteractionResult.FAIL;
        }
        CollectorBackpackData.link(backpack, core, level);
        if (player instanceof ServerPlayer serverPlayer) CollectorAdvancementEvents.onBackpackLink(serverPlayer);
        if (!player.getAbilities().instabuild) key.shrink(1);
        player.displayClientMessage(Component.translatable("message.mdkcosmetic.backpack_linked"), true);
        return InteractionResult.SUCCESS;
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (CollectorBackpackData.linked(stack)) tooltip.add(Component.translatable("tooltip.mdkcosmetic.backpack_linked"));
        else tooltip.add(Component.translatable("tooltip.mdkcosmetic.backpack_capacity", CollectorBackpackData.capacity(stack)));
        tooltip.add(Component.translatable("tooltip.mdkcosmetic.backpack_modules",
                CollectorBackpackData.hasMagnet(stack, context.registries())
                        ? Component.translatable("tooltip.mdkcosmetic.installed")
                        : Component.translatable("tooltip.mdkcosmetic.not_installed"),
                CollectorBackpackData.hasFilter(stack, context.registries())
                        ? Component.translatable("tooltip.mdkcosmetic.installed")
                        : Component.translatable("tooltip.mdkcosmetic.not_installed")));
        tooltip.add(Component.translatable("tooltip.mdkcosmetic.backpack_equip_hint"));
        if (stack.is(MDKCosmetic.COLLECTOR_BACKPACK_BASIC.get()))
            tooltip.add(Component.translatable("tooltip.mdkcosmetic.backpack_upgrade_cost"));
    }
}
