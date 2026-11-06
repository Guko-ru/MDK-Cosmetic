package com.mdk.cosmetic.gametest.legacy;


import org.slf4j.LoggerFactory;
public final class LegacyRegressionInitializer  {
 public static void initialize(){
  if(!Boolean.getBoolean("mdkcosmetic.clientRegressions")&&!Boolean.getBoolean("mdkcosmetic.clientSmoke"))return;
  boolean[] started={false};
  net.minecraftforge.event.TickEvent.ClientTickEvent.Post.BUS.addListener((net.minecraftforge.event.TickEvent.ClientTickEvent.Post event)->{var client=net.minecraft.client.Minecraft.getInstance();
   client.options.pauseOnLostFocus=false;
   LegacyClientContext.TICKS.incrementAndGet();
   if(!Boolean.getBoolean("mdkcosmetic.clientRegressions"))return;
   if(started[0]||client.getOverlay()!=null||client.screen==null||LegacyClientContext.TICKS.get()<20)return;
   started[0]=true;
   new Thread(()->{try{new com.mdk.cosmetic.gametest.CollectorClientRegressionTest().runTest(new LegacyClientContext());client.execute(client::stop);}catch(Throwable error){LoggerFactory.getLogger("mdkcosmetic-test").error("MDK_CLIENT_REGRESSIONS_FAILED",error);client.execute(()->{throw new AssertionError("Client regression failed",error);});}},"MDK legacy client tests").start();
  });
 }
}
