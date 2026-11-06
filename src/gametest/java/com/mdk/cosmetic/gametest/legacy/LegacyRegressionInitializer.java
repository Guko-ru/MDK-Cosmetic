package com.mdk.cosmetic.gametest.legacy;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.LoggerFactory;
public final class LegacyRegressionInitializer implements ClientModInitializer {
 @Override public void onInitializeClient(){
  if(!Boolean.getBoolean("mdkcosmetic.clientRegressions"))return;
  boolean[] started={false};
  ClientTickEvents.END_CLIENT_TICK.register(client->{
   LegacyClientContext.TICKS.incrementAndGet();
   if(started[0]||client.getOverlay()!=null||client.screen==null||LegacyClientContext.TICKS.get()<20)return;
   started[0]=true;
   new Thread(()->{try{new com.mdk.cosmetic.gametest.CollectorClientRegressionTest().runTest(new LegacyClientContext());client.execute(client::stop);}catch(Throwable error){LoggerFactory.getLogger("mdkcosmetic-test").error("MDK_CLIENT_REGRESSIONS_FAILED",error);client.execute(()->{throw new AssertionError("Client regression failed",error);});}},"MDK legacy client tests").start();
  });
 }
}
