package com.mdk.cosmetic.gametest;

import com.mdk.cosmetic.*;
import com.mdk.cosmetic.client.*;
import com.mdk.cosmetic.gametest.legacy.LegacyClientContext;



import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Field;
import java.util.UUID;

/** Real integrated-server regression checks for the reported machine, creative and boss bugs. */
public final class CollectorClientRegressionTest implements LegacyClientContext.Fixture {
 private static final BlockPos CENTER=new BlockPos(0,100,0),LINK=CENTER.north();
 private UUID bossId;
 private static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 private static Object field(Object object,String name){
  for(Class<?> type=object.getClass();type!=null;type=type.getSuperclass())try{Field f=type.getDeclaredField(name);f.setAccessible(true);return f.get(object);}catch(NoSuchFieldException ignored){}catch(ReflectiveOperationException error){throw new AssertionError(error);}
  throw new AssertionError("Missing field "+name);
 }
 private static void selectTab(CreativeModeInventoryScreen screen,net.minecraft.world.item.CreativeModeTab tab){
  var pages=(java.util.List<net.neoforged.neoforge.client.gui.CreativeTabsScreenPage>)field(screen,"pages");
  var page=pages.stream().filter(p->p.getVisibleTabs().contains(tab)).findFirst().orElseThrow();screen.setCurrentPage(page);
  try{var select=CreativeModeInventoryScreen.class.getDeclaredMethod("selectTab",net.minecraft.world.item.CreativeModeTab.class);select.setAccessible(true);select.invoke(screen,tab);require(field(screen,"selectedTab")==tab,"Native creative tab selection failed");}catch(ReflectiveOperationException error){throw new AssertionError(error);}
 }
 private static void closeMenu(LegacyClientContext context,LegacyClientContext.World world){world.getServer().runOnServer(server->server.getPlayerList().getPlayers().getFirst().closeContainer());context.waitForScreen(null);}
 private static void openLink(LegacyClientContext context,LegacyClientContext.World world){
  world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();p.teleportTo(.5,100,-3.5);p.openMenu((CollectorLinkBlockEntity)p.serverLevel().getBlockEntity(LINK));});
  context.waitForScreen(CollectorLinkScreen.class);context.waitTicks(5);
 }
 private static double[] position(LegacyClientContext context,int x,int y){return context.computeOnClient(client->{var screen=(CollectorLinkScreen)client.screen;float scale=(float)field(screen,"uiScale");int left=(int)field(screen,"leftPos"),top=(int)field(screen,"topPos");return new double[]{(left+x)*scale*client.getWindow().getGuiScale(),(top+y)*scale*client.getWindow().getGuiScale()};});}
 private static void point(LegacyClientContext context,int x,int y){var p=position(context,x,y);context.getInput().setCursorPos(p[0],p[1]);context.waitTick();}
 private static void click(LegacyClientContext context,int x,int y){point(context,x,y);context.getInput().pressMouse(0);context.waitTicks(2);}
 private static void capture(LegacyClientContext context,String name){context.runOnClient(client->client.getToastManager().clear());context.getInput().setCursorPos(0,0);context.waitTicks(2);context.takeScreenshot(name);}
 private static void checkSlots(LegacyClientContext context,int start,int end){
  for(int i=start;i<end;i++){
   final int index=i;int[] xy=context.computeOnClient(client->{Slot s=((CollectorLinkScreen)client.screen).getMenu().getSlot(index);return new int[]{s.x+8,s.y+8};});
   point(context,xy[0],xy[1]);
   context.runOnClient(client->{var screen=(CollectorLinkScreen)client.screen;require(field(screen,"hoveredSlot")==screen.getMenu().getSlot(index),"Rendered slot and native hover/click region differ at slot "+index);});
  }
 }
 @Override public void runTest(LegacyClientContext context){
  try(var world=context.worldBuilder().create()){
   world.getClientWorld().waitForChunksRender();
   world.getServer().runOnServer(server->{
    var p=server.getPlayerList().getPlayers().getFirst();var level=p.serverLevel();p.setGameMode(GameType.CREATIVE);p.setInvulnerable(true);p.teleportTo(.5,100,-3.5);
    for(int x=-4;x<=4;x++)for(int z=-7;z<=-3;z++)level.setBlock(new BlockPos(x,99,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
    for(int x=10;x<=15;x++)for(int z=-6;z<=2;z++)level.setBlock(new BlockPos(x,100,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
    for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++)level.setBlock(CENTER.offset(x,y,z),MDKCosmetic.COLLECTOR_CASING.get().defaultBlockState(),3);
    level.setBlock(CENTER,MDKCosmetic.COLLECTOR_SOUL_STORAGE.get().defaultBlockState(),3);
    level.setBlock(LINK,MDKCosmetic.COLLECTOR_LINK.get().defaultBlockState().setValue(CollectorLinkBlock.FACING,Direction.NORTH),3);
    for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(i==0?Items.DIAMOND:i%2==0?MDKCosmetic.COLLECTOR_MEMORY_CASING_ITEM.get():Items.GOLD_INGOT,16+i));
   });context.waitTicks(35);openLink(context,world);
   int[][] windows={{854,480,2},{1280,720,3},{1920,1080,4},{854,480,1}};
   for(int[] window:windows){
    context.getInput().resizeWindow(window[0],window[1]);context.runOnClient(client->{client.options.guiScale().set(window[2]);client.resizeDisplay();});context.waitTicks(5);
    context.runOnClient(client->{var s=(CollectorLinkScreen)client.screen;float scale=(float)field(s,"uiScale");int left=(int)field(s,"leftPos"),top=(int)field(s,"topPos");require(left*scale>=0&&top*scale>=0&&(left+606)*scale<=s.width&&(top+308)*scale<=s.height,"Machine window escapes viewport");require(s.getMenu().isActive()&&!s.getMenu().isMemoryReady(),"Tier-one structure must be active with memory locked");});
    checkSlots(context,CollectorLinkMenu.PLAYER_START,CollectorLinkMenu.PLAYER_END);
    click(context,224,40);context.runOnClient(client->require((int)field(client.screen,"category")==CollectorOffers.MISC,"Misc category button missed its drawn position"));
    click(context,100,40);context.runOnClient(client->require((int)field(client.screen,"category")==CollectorOffers.EXCLUSIVE,"Exclusive category button missed its drawn position"));
    click(context,69,281);context.runOnClient(client->require(!((CollectorLinkScreen)client.screen).getMenu().getCarried().isEmpty(),"Click on drawn hotbar slot did not pick up its item"));context.waitTicks(8);click(context,69,281);context.runOnClient(client->require(((CollectorLinkScreen)client.screen).getMenu().getCarried().isEmpty(),"Returning the stack to its native slot failed"));
    capture(context,"collector-tier1-"+window[0]+"-scale"+window[2]);
   }
   closeMenu(context,world);
   world.getServer().runOnServer(server->{
    var level=server.getPlayerList().getPlayers().getFirst().level();
    for(int x=-2;x<=2;x++)for(int y=-2;y<=2;y++)for(int z=-2;z<=2;z++){
     var pos=CENTER.offset(x,y,z);
     if(x==0&&y==-2&&z==0)level.setBlock(pos,MDKCosmetic.COLLECTOR_MEMORY_CORE.get().defaultBlockState(),3);
     else if(CollectorMemoryStructure.isCasing(x,y,z))level.setBlock(pos,MDKCosmetic.COLLECTOR_MEMORY_CASING.get().defaultBlockState(),3);
     else if(CollectorMemoryStructure.isOpenSpace(x,y,z))level.setBlock(pos,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
    }
    var core=(CollectorMemoryCoreBlockEntity)level.getBlockEntity(CENTER.below(2));core.deposit(new ItemStack(Items.DIAMOND,64));core.deposit(new ItemStack(MDKCosmetic.COLLECTOR_SWORD.get()));
    int count=0;for(var item:BuiltInRegistries.ITEM){if(item==Items.AIR||item==Items.DIAMOND)continue;core.deposit(new ItemStack(item,Math.min(64,item.getDefaultMaxStackSize())));if(++count>=70)break;}
   });context.waitTicks(30);openLink(context,world);
   for(int[] window:windows){
    context.getInput().resizeWindow(window[0],window[1]);context.runOnClient(client->{client.options.guiScale().set(window[2]);client.resizeDisplay();});context.waitTicks(5);
    context.runOnClient(client->{var menu=((CollectorLinkScreen)client.screen).getMenu();require(menu.isMemoryReady()&&menu.getMemoryTotal()>48,"Second-tier memory did not synchronize");for(int i=0;i<48;i++)require(!menu.getSlot(CollectorLinkMenu.MEMORY_START+i).getItem().isEmpty(),"Memory preview slot missing: "+i);});
    checkSlots(context,CollectorLinkMenu.MEMORY_START,CollectorLinkMenu.MEMORY_START+48);
    point(context,320,100);context.getInput().holdShift();context.getInput().pressMouse(0);context.getInput().releaseShift();context.waitTicks(3);
    capture(context,"collector-tier2-"+window[0]+"-scale"+window[2]);
   }
   closeMenu(context,world);context.runOnClient(client->client.setScreen(new CreativeModeInventoryScreen(client.player,client.player.connection.enabledFeatures(),true)));context.waitForScreen(CreativeModeInventoryScreen.class);context.waitTicks(5);
   context.runOnClient(client->{var screen=(CreativeModeInventoryScreen)client.screen;selectTab(screen,MDKCosmetic.COSMETIC_TAB.get());require(((java.util.List<net.neoforged.neoforge.client.gui.CreativeTabsScreenPage>)field(screen,"pages")).stream().anyMatch(page->page.getVisibleTabs().contains(MDKCosmetic.FUR_BLOCKS_TAB.get())),"Decor tab missing from MDK creative page");require(MDKCosmetic.COSMETIC_TAB.get().getDisplayItems().size()>60&&MDKCosmetic.FUR_BLOCKS_TAB.get().getDisplayItems().size()>1000,"Creative tab contents missing");});
   capture(context,"collector-creative-cosmetics");context.runOnClient(client->selectTab((CreativeModeInventoryScreen)client.screen,MDKCosmetic.FUR_BLOCKS_TAB.get()));context.waitTicks(3);capture(context,"collector-creative-decor");
   context.runOnClient(client->client.setScreen(null));
   world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();p.teleportTo(12.5,101,-4);p.setYRot(0);p.setXRot(0);var boss=MDKCosmetic.UNO_ENTITY.get().create(p.serverLevel(),EntitySpawnReason.TRIGGERED);boss.snapTo(12.5,101,0,180,0);boss.setNoAi(true);boss.setNoGravity(true);boss.setPersistenceRequired();p.serverLevel().addFreshEntity(boss);bossId=boss.getUUID();require(boss.getHealth()==400&&!boss.isSecondPhase(),"New boss must start with full health in its original phase");});
   context.waitFor(client->client.level!=null&&client.level.entitiesForRendering().iterator().hasNext());context.waitTicks(20);
   context.runOnClient(client->{UnoEntity boss=null;for(var entity:client.level.entitiesForRendering())if(entity.getUUID().equals(bossId))boss=(UnoEntity)entity;require(boss!=null&&!boss.isSecondPhase(),"Fresh boss must remain in black phase");var dispatcher=client.getEntityRenderDispatcher();var state=(UnoRenderState)((UnoRenderer)dispatcher.getRenderer(boss)).createRenderState(boss,0F);require(dispatcher.getRenderer(boss) instanceof UnoRenderer,"Native scene dispatch changed boss to a player renderer");require(state.texture.getPath().equals("textures/entity/uno.png"),"Fresh boss texture must be the black original");});
   capture(context,"collector-heart-black-phase");
   world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();p.setGameMode(GameType.SURVIVAL);p.setInvulnerable(true);p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.DIAMOND_HELMET));p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE));var boss=(UnoEntity)p.serverLevel().getEntity(bossId);boss.setHealth(199);});context.waitTicks(20);
   world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();var boss=(UnoEntity)p.serverLevel().getEntity(bossId);require(boss.isSecondPhase()&&boss.getMimicTarget().orElseThrow().equals(p.getUUID()),"Second phase must copy the actual opponent");require(ItemStack.isSameItemSameComponents(boss.getItemBySlot(EquipmentSlot.HEAD),p.getItemBySlot(EquipmentSlot.HEAD)),"Second phase must copy opponent equipment");});
   context.runOnClient(client->{UnoEntity boss=null;for(var entity:client.level.entitiesForRendering())if(entity.getUUID().equals(bossId))boss=(UnoEntity)entity;var state=(UnoRenderState)((UnoRenderer)client.getEntityRenderDispatcher().getRenderer(boss)).createRenderState(boss,0F);require(state.texture.equals(client.player.getSkin().texture()),"Second phase must render the copied player skin");});
   world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();p.teleportTo(12.5,101,-4);p.setYRot(0);p.setXRot(0);var boss=(UnoEntity)p.serverLevel().getEntity(bossId);boss.snapTo(12.5,101,0,180,0);boss.setDeltaMovement(Vec3.ZERO);});context.waitTicks(3);capture(context,"collector-heart-mimic-phase");
   world.getServer().runOnServer(server->{
    var player=server.getPlayerList().getPlayers().getFirst();var level=player.serverLevel();var boss=level.getEntity(bossId);if(boss!=null)boss.discard();player.teleportTo(12.5,101,-5.5);player.setYRot(0);player.setXRot(0);
    var hats=java.util.List.of(MDKCosmetic.FOX_HAT.get(),MDKCosmetic.CAT_EARS.get(),MDKCosmetic.SAKURA_THINKER.get());int[] colors={0xE95855,0x35BCD4,0xB469E4};
    for(int actor=0;actor<3;actor++){
     var stand=new net.minecraft.world.entity.decoration.ArmorStand(level,11+actor*1.5,101,0);stand.setNoGravity(true);stand.setShowArms(true);stand.setNoBasePlate(true);stand.setYRot(180);
     stand.setHeadPose(new net.minecraft.core.Rotations(0,(actor-1)*15,0));stand.setLeftArmPose(new net.minecraft.core.Rotations(actor*12,0,-5));stand.setRightArmPose(new net.minecraft.core.Rotations(-actor*12,0,5));
     var pieces=java.util.List.of(hats.get(actor),MDKCosmetic.COLLECTOR_CHESTPLATE.get(),MDKCosmetic.COLLECTOR_LEGGINGS.get(),MDKCosmetic.COLLECTOR_BOOTS.get());var slots=new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
     for(int part=0;part<4;part++){
      var stack=new ItemStack(pieces.get(part));stack.set(net.minecraft.core.component.DataComponents.DYED_COLOR,new net.minecraft.world.item.component.DyedItemColor(colors[actor]));if(actor==1)stack.set(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE,true);
      if(actor==2&&part==1)stack.set(net.minecraft.core.component.DataComponents.TRIM,new net.minecraft.world.item.equipment.trim.ArmorTrim(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.TRIM_MATERIAL).getOrThrow(net.minecraft.world.item.equipment.trim.TrimMaterials.DIAMOND),level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.TRIM_PATTERN).getOrThrow(net.minecraft.world.item.equipment.trim.TrimPatterns.SENTRY)));
      stand.setItemSlot(slots[part],stack);
     }
     level.addFreshEntity(stand);
    }
   });context.waitTicks(20);capture(context,"native-armor-hats-dye-glint-trim");
   LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_NATIVE_ARMOR_SCENE_OK: 3 equipped actors, hats, dye, glint and trim rendered");
   LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_CLIENT_REGRESSIONS_OK: two machine tiers, four GUI configurations, native slot hits, creative tabs and both boss phases");
  }
 }
}
