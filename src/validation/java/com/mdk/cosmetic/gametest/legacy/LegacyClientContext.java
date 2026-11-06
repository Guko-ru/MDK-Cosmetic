package com.mdk.cosmetic.gametest.legacy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import java.nio.file.Files;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.lwjgl.glfw.GLFW;

/** Native-client test adapter for versions predating Fabric's client gametest API. Test mod only. */
public final class LegacyClientContext {
 public interface Fixture {void runTest(LegacyClientContext context);}
 public static final AtomicLong TICKS=new AtomicLong();
 public static final Set<Integer> HELD=ConcurrentHashMap.newKeySet();
 private final Minecraft client=Minecraft.getInstance();
 public void runOnClient(Consumer<Minecraft> action){client.submit(()->action.accept(client)).join();}
 public <T>T computeOnClient(Function<Minecraft,T> action){return client.submit(()->action.apply(client)).join();}
 private static void pause(){try{Thread.sleep(2);}catch(InterruptedException error){Thread.currentThread().interrupt();throw new AssertionError(error);}}
 public void waitTick(){waitTicks(1);}
 public void waitTicks(int count){long end=TICKS.get()+count,deadline=System.nanoTime()+120_000_000_000L;while(TICKS.get()<end){if(System.nanoTime()>deadline)throw new AssertionError("Client stopped ticking");pause();}}
 public int waitFor(Predicate<Minecraft> predicate){for(int tick=0;tick<2400;tick++){if(computeOnClient(predicate::test))return tick;waitTick();}throw new AssertionError("Client condition timed out");}
 public int waitForScreen(Class<? extends Screen> screen){return waitFor(c->screen==null?c.screen==null:screen.isInstance(c.screen));}
 public void clickScreenButton(String key){runOnClient(c->{for(var child:c.screen.children())if(child instanceof Button b&&b.getMessage().getString().equals(net.minecraft.client.resources.language.I18n.get(key))){b.onPress();return;}throw new AssertionError("Button missing: "+key);});waitTick();}
 public void takeScreenshot(String name){
  var saved=new java.util.concurrent.CompletableFuture<Void>();
  runOnClient(c->net.minecraft.client.Screenshot.takeScreenshot(c.getMainRenderTarget(),image->{try(image){var out=c.gameDirectory.toPath().resolve("screenshots");Files.createDirectories(out);image.writeToFile(out.resolve(name+".png"));saved.complete(null);}catch(Exception error){saved.completeExceptionally(error);}}));
  saved.join();
 }

 public Input getInput(){return new Input();}
 public WorldBuilder worldBuilder(){return new WorldBuilder();}
 public final class WorldBuilder {
  public World create(){
   runOnClient(c->c.createWorldOpenFlows().createFreshLevel("MDK Regression "+System.currentTimeMillis(),new LevelSettings("MDK regression",GameType.CREATIVE,false,Difficulty.NORMAL,true,new GameRules(net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS),WorldDataConfiguration.DEFAULT),new WorldOptions(0L,false,false),registries->registries.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),new TitleScreen()));
   waitFor(c->c.level!=null&&c.player!=null&&c.getSingleplayerServer()!=null&&c.screen==null);waitTicks(40);return new World();
  }
 }
 public final class World implements AutoCloseable {
  public Server getServer(){return new Server();}
  public ClientWorld getClientWorld(){return new ClientWorld();}
  @Override public void close(){runOnClient(c->{if(c.level!=null)c.level.disconnect();c.disconnect(new TitleScreen());});waitFor(c->c.level==null);}
 }
 public final class ClientWorld {public void waitForChunksRender(){waitTicks(40);}}
 public final class Server {
  public void runOnServer(Consumer<MinecraftServer> action){var server=computeOnClient(Minecraft::getSingleplayerServer);server.submit(()->action.accept(server)).join();}
 }
 private static void invoke(Object target,String name,Class<?>[] types,Object...args){try{var method=target.getClass().getDeclaredMethod(name,types);method.setAccessible(true);method.invoke(target,args);}catch(ReflectiveOperationException error){throw new AssertionError(error);}}
 public final class Input {
  public void setCursorPos(double x,double y){runOnClient(c->invoke(c.mouseHandler,"onMove",new Class<?>[]{long.class,double.class,double.class},c.getWindow().getWindow(),x,y));}
  public void pressMouse(int button){runOnClient(c->invoke(c.mouseHandler,"onPress",new Class<?>[]{long.class,int.class,int.class,int.class},c.getWindow().getWindow(),button,GLFW.GLFW_PRESS,0));waitTick();runOnClient(c->invoke(c.mouseHandler,"onPress",new Class<?>[]{long.class,int.class,int.class,int.class},c.getWindow().getWindow(),button,GLFW.GLFW_RELEASE,0));}
  public void holdShift(){HELD.add(GLFW.GLFW_KEY_LEFT_SHIFT);}
  public void releaseShift(){HELD.remove(GLFW.GLFW_KEY_LEFT_SHIFT);}
  public void resizeWindow(int width,int height){runOnClient(c->GLFW.glfwSetWindowSize(c.getWindow().getWindow(),width,height));waitTicks(4);}
 }
}
