package com.mdk.cosmetic.platform;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Mod-owned event contexts. Fabric callbacks and mixins dispatch these in original listener order. */
public final class Events {
    private Events() {}
    public static class Event {
        private boolean canceled;
        public boolean isCanceled(){return canceled;}
        public void setCanceled(boolean canceled){this.canceled=canceled;}
    }
    public static class EntityEvent extends Event {
        private final Entity entity;
        public EntityEvent(Entity entity){this.entity=entity;}
        public Entity getEntity(){return entity;}
    }
    public static class LivingEvent extends EntityEvent {
        public LivingEvent(LivingEntity entity){super(entity);}
        @Override public LivingEntity getEntity(){return (LivingEntity)super.getEntity();}
    }
    public static class PlayerEvent extends EntityEvent {
        public PlayerEvent(Player player){super(player);}
        @Override public Player getEntity(){return (Player)super.getEntity();}
        public static final class Clone extends PlayerEvent {
            private final Player original;
            public Clone(Player player,Player original){super(player);this.original=original;}
            public Player getOriginal(){return original;}
        }
        public static final class PlayerLoggedInEvent extends PlayerEvent { public PlayerLoggedInEvent(Player p){super(p);} }
        public static final class PlayerRespawnEvent extends PlayerEvent { public PlayerRespawnEvent(Player p){super(p);} }
        public static final class ItemCraftedEvent extends PlayerEvent {
            private final ItemStack crafting;
            public ItemCraftedEvent(Player p,ItemStack crafting){super(p);this.crafting=crafting;}
            public ItemStack getCrafting(){return crafting;}
        }
        public static final class BreakSpeed extends PlayerEvent {
            private final BlockState state;
            private float speed;
            public BreakSpeed(Player p,BlockState state,float speed){super(p);this.state=state;this.speed=speed;}
            public BlockState getState(){return state;}
            public float getNewSpeed(){return speed;}
            public void setNewSpeed(float speed){this.speed=speed;}
        }
    }
    public static final class EntityJoinLevelEvent extends EntityEvent {
        public EntityJoinLevelEvent(Entity e){super(e);}
        public Level getLevel(){return getEntity().level();}
    }
    public static class EntityTickEvent extends EntityEvent {
        public EntityTickEvent(Entity e){super(e);}
        public static final class Post extends EntityTickEvent { public Post(Entity e){super(e);} }
    }
    public static class PlayerTickEvent extends PlayerEvent {
        public PlayerTickEvent(Player p){super(p);}
        public static final class Post extends PlayerTickEvent { public Post(Player p){super(p);} }
    }
    public static final class ServerTickEvent {
        public record Post(MinecraftServer getServer) {}
    }
    public static final class PlayerInteractEvent {
        public static final class EntityInteract extends PlayerEvent {
            private final Entity target;
            private final InteractionHand hand;
            private InteractionResult result=InteractionResult.PASS;
            public EntityInteract(Player p,Entity target,InteractionHand hand){super(p);this.target=target;this.hand=hand;}
            public Entity getTarget(){return target;}
            public InteractionHand getHand(){return hand;}
            public InteractionResult getCancellationResult(){return result;}
            public void setCancellationResult(InteractionResult result){this.result=result;}
        }
    }
    public static final class LivingDropsEvent extends LivingEvent {
        private final DamageSource source;
        private final List<ItemEntity> drops;
        public LivingDropsEvent(LivingEntity e,DamageSource source,List<ItemEntity> drops){super(e);this.source=source;this.drops=drops;}
        public DamageSource getSource(){return source;}
        public List<ItemEntity> getDrops(){return drops;}
    }
    public static final class LivingDeathEvent extends LivingEvent {
        private final DamageSource source;
        public LivingDeathEvent(LivingEntity e,DamageSource source){super(e);this.source=source;}
        public DamageSource getSource(){return source;}
    }
    public static final class LivingFallEvent extends LivingEvent { public LivingFallEvent(LivingEntity e){super(e);} }
    public static final class LivingChangeTargetEvent extends LivingEvent {
        private final LivingEntity target;
        public LivingChangeTargetEvent(LivingEntity e,LivingEntity target){super(e);this.target=target;}
        public LivingEntity getNewAboutToBeSetTarget(){return target;}
    }
    public static final class LivingDamageEvent {
        public static final class Pre extends LivingEvent {
            private final DamageSource source; private float amount;
            public Pre(LivingEntity e,DamageSource source,float amount){super(e);this.source=source;this.amount=amount;}
            public DamageSource getSource(){return source;}
            public float getNewDamage(){return amount;}
            public void setNewDamage(float amount){this.amount=amount;}
        }
    }
    public static final class EntityInvulnerabilityCheckEvent extends EntityEvent {
        private final DamageSource source;private boolean invulnerable;
        public EntityInvulnerabilityCheckEvent(Entity e,DamageSource source,boolean original){super(e);this.source=source;invulnerable=original;}
        public DamageSource getSource(){return source;}
        public void setInvulnerable(boolean value){invulnerable=value;}
        public boolean isInvulnerable(){return invulnerable;}
    }
    public static final class ExplosionEvent {
        public record Detonate(Level getLevel,List<BlockPos> getAffectedBlocks) {}
    }
    public static final class BlockEvent {
        public static final class BreakEvent extends Event {
            private final Player player;private final BlockState state;
            public BreakEvent(Player player,BlockState state){this.player=player;this.state=state;}
            public Player getPlayer(){return player;}
            public BlockState getState(){return state;}
        }
    }
    public record BlockDropsEvent(BlockState getState,Entity getBreaker,ItemStack getTool,BlockPos getPos,List<ItemEntity> getDrops) {}
    public static final class ItemEntityPickupEvent {
        public record Post(Player getPlayer,ItemStack getOriginalStack,ItemStack getCurrentStack) {}
    }
    public static final class ItemAttributeModifierEvent {
        private final ItemStack stack;private final List<ItemAttributeModifiers.Entry> entries;
        public ItemAttributeModifierEvent(ItemStack stack,ItemAttributeModifiers original){this.stack=stack;this.entries=new ArrayList<>(original.modifiers());}
        public ItemStack getItemStack(){return stack;}
        public void addModifier(Holder<Attribute> attribute,AttributeModifier modifier,EquipmentSlotGroup slot){entries.add(new ItemAttributeModifiers.Entry(attribute,modifier,slot));}
        public ItemAttributeModifiers result(boolean show){return new ItemAttributeModifiers(List.copyOf(entries));}
    }
    public static void dispatch(Object event){
        if(event instanceof EntityJoinLevelEvent value){
            com.mdk.cosmetic.CollectorProtection.onItemSpawn(value);
            com.mdk.cosmetic.AnimalHatAttraction.onEntityJoin(value);
            com.mdk.cosmetic.HostileHatSpawns.onJoin(value);
            com.mdk.cosmetic.CollectorArmorFear.onMobJoin(value);
        }
        if(event instanceof EntityInvulnerabilityCheckEvent value){
            com.mdk.cosmetic.CollectorProtection.onInvulnerabilityCheck(value);
        }
        if(event instanceof ExplosionEvent.Detonate value){
            com.mdk.cosmetic.CollectorProtection.onExplosion(value);
        }
        if(event instanceof BlockEvent.BreakEvent value){
            if(!value.isCanceled()) com.mdk.cosmetic.CollectorMeteorBlock.onBreak(value);
            if(!value.isCanceled()) com.mdk.cosmetic.CollectorVoidAchievements.onBlockBreak(value);
            if(!value.isCanceled()) com.mdk.cosmetic.CollectorOres.onBreak(value);
        }
        if(event instanceof PlayerTickEvent.Post value){
            com.mdk.cosmetic.CollectorEquipmentEffects.onTick(value);
            com.mdk.cosmetic.CollectorBackpackMagnet.onTick(value);
            com.mdk.cosmetic.CollectorBackpackFurnace.onTick(value);
            com.mdk.cosmetic.CollectorAdvancementEvents.onTick(value);
            com.mdk.cosmetic.RareTraderHatEffects.onPlayerTick(value);
            com.mdk.cosmetic.CollectorSwordWave.onPlayerTick(value);
            com.mdk.cosmetic.AllayHatGlow.onPlayerTick(value);
            com.mdk.cosmetic.AxolotlHatBubbles.onPlayerTick(value);
            com.mdk.cosmetic.GlowSquidHatGlow.onPlayerTick(value);
        }
        if(event instanceof LivingFallEvent value){
            com.mdk.cosmetic.CollectorEquipmentEffects.onFall(value);
        }
        if(event instanceof PlayerEvent.Clone value){
            com.mdk.cosmetic.CollectorEquipmentEffects.onClone(value);
        }
        if(event instanceof PlayerEvent.PlayerLoggedInEvent value){
            com.mdk.cosmetic.CollectorEquipmentEffects.onLogin(value);
        }
        if(event instanceof PlayerEvent.PlayerRespawnEvent value){
            com.mdk.cosmetic.CollectorEquipmentEffects.onRespawn(value);
        }
        if(event instanceof LivingDropsEvent value){
            if(!value.isCanceled()) com.mdk.cosmetic.CollectorEquipmentEffects.onDrops(value);
            if(!value.isCanceled()) com.mdk.cosmetic.CollectorBackpackDeathVault.onDrops(value);
            if(!value.isCanceled()) com.mdk.cosmetic.FoxFurShearing.onFoxDrops(value);
            if(!value.isCanceled()) com.mdk.cosmetic.PandaFurShearing.onPandaDrops(value);
            if(!value.isCanceled()) com.mdk.cosmetic.CatFurShearing.onCatDrops(value);
            if(!value.isCanceled()) com.mdk.cosmetic.CollectorEssenceDrops.onDrops(value);
            if(!value.isCanceled()) com.mdk.cosmetic.UnoMemoryDrops.onDrops(value);
            if(!value.isCanceled()) com.mdk.cosmetic.AllayFiberHarvest.onDrops(value);
            if(!value.isCanceled()) com.mdk.cosmetic.AxolotlSkinHarvest.onDrops(value);
            if(!value.isCanceled()) com.mdk.cosmetic.RabbitFurShearing.onRabbitDrops(value);
            if(!value.isCanceled()) com.mdk.cosmetic.SquidInkHarvest.onDrops(value);
            if(!value.isCanceled()) com.mdk.cosmetic.GlowSquidInkHarvest.onDrops(value);
            if(!value.isCanceled()) com.mdk.cosmetic.WolfFurShearing.onWolfDrops(value);
            if(!value.isCanceled()) com.mdk.cosmetic.HorseFurShearing.onDrops(value);
            if(!value.isCanceled()) com.mdk.cosmetic.DonkeyFurShearing.onDrops(value);
            if(!value.isCanceled()) com.mdk.cosmetic.CamelFurShearing.onCamelDrops(value);
            if(!value.isCanceled()) com.mdk.cosmetic.CowFurShearing.onCowDrops(value);
            if(!value.isCanceled()) com.mdk.cosmetic.MooshroomFurShearing.onDrops(value);
            if(!value.isCanceled()) com.mdk.cosmetic.PolarBearFurShearing.onDrops(value);
            if(!value.isCanceled()) com.mdk.cosmetic.LlamaFurShearing.onDrops(value);
        }
        if(event instanceof LivingDeathEvent value){
            if(!value.isCanceled()) com.mdk.cosmetic.CollectorBackpackTotem.onDeath(value);
            if(!value.isCanceled()) com.mdk.cosmetic.CollectorAdvancementEvents.onKill(value);
        }
        if(event instanceof EntityTickEvent.Post value){
            com.mdk.cosmetic.CollectorHeartWorldEvents.onEntityTick(value);
            com.mdk.cosmetic.UnoDragonEncounter.onEntityTick(value);
            com.mdk.cosmetic.CollectorSoulWorldEvents.onWardenTick(value);
            com.mdk.cosmetic.CollectorArmorFear.onMobTick(value);
            com.mdk.cosmetic.AllayHatAttraction.onAllayTick(value);
            com.mdk.cosmetic.AxolotlHatAttraction.onAxolotlTick(value);
            com.mdk.cosmetic.SquidHatAttraction.onSquidTick(value);
            com.mdk.cosmetic.GlowSquidHatAttraction.onGlowSquidTick(value);
            com.mdk.cosmetic.CamelHatAttraction.onCamelTick(value);
        }
        if(event instanceof LivingDamageEvent.Pre value){
            com.mdk.cosmetic.CollectorHeartWorldEvents.onHurt(value);
        }
        if(event instanceof ServerTickEvent.Post value){
            com.mdk.cosmetic.CollectorSoulWorldEvents.onServerTick(value);
        }
        if(event instanceof LivingChangeTargetEvent value){
            if(!value.isCanceled()) com.mdk.cosmetic.CollectorSoulWorldEvents.onWardenChangeTarget(value);
            if(!value.isCanceled()) com.mdk.cosmetic.CollectorArmorFear.onTargetChange(value);
        }
        if(event instanceof PlayerInteractEvent.EntityInteract value){
            if(!value.isCanceled()) com.mdk.cosmetic.FoxFurShearing.onInteract(value);
            if(!value.isCanceled()) com.mdk.cosmetic.PandaFurShearing.onInteract(value);
            if(!value.isCanceled()) com.mdk.cosmetic.CatFurShearing.onInteract(value);
            if(!value.isCanceled()) com.mdk.cosmetic.CollectorWanderingTrader.onInteract(value);
            if(!value.isCanceled()) com.mdk.cosmetic.AllayFiberHarvest.onInteract(value);
            if(!value.isCanceled()) com.mdk.cosmetic.AxolotlSkinHarvest.onInteract(value);
            if(!value.isCanceled()) com.mdk.cosmetic.RabbitFurShearing.onInteract(value);
            if(!value.isCanceled()) com.mdk.cosmetic.SquidInkHarvest.onInteract(value);
            if(!value.isCanceled()) com.mdk.cosmetic.GlowSquidInkHarvest.onInteract(value);
            if(!value.isCanceled()) com.mdk.cosmetic.WolfFurShearing.onInteract(value);
            if(!value.isCanceled()) com.mdk.cosmetic.HorseFurShearing.onInteract(value);
            if(!value.isCanceled()) com.mdk.cosmetic.DonkeyFurShearing.onInteract(value);
            if(!value.isCanceled()) com.mdk.cosmetic.CamelFurShearing.onInteract(value);
            if(!value.isCanceled()) com.mdk.cosmetic.CowFurShearing.onInteract(value);
            if(!value.isCanceled()) com.mdk.cosmetic.MooshroomFurShearing.onInteract(value);
            if(!value.isCanceled()) com.mdk.cosmetic.PolarBearFurShearing.onInteract(value);
            if(!value.isCanceled()) com.mdk.cosmetic.LlamaFurShearing.onInteract(value);
        }
        if(event instanceof ItemEntityPickupEvent.Post value){
            com.mdk.cosmetic.CollectorAdvancementEvents.onPickup(value);
        }
        if(event instanceof PlayerEvent.ItemCraftedEvent value){
            com.mdk.cosmetic.CollectorAdvancementEvents.onCraft(value);
        }
        if(event instanceof ItemAttributeModifierEvent value){
            com.mdk.cosmetic.RareTraderHatEffects.onAttributeModifiers(value);
        }
        if(event instanceof PlayerEvent.BreakSpeed value){
            com.mdk.cosmetic.CollectorOres.onBreakSpeed(value);
        }
        if(event instanceof BlockDropsEvent value){
            com.mdk.cosmetic.CollectorOres.onDrops(value);
        }
    }

}
