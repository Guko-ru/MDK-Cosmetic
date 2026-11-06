package com.mdk.cosmetic.gametest;
@net.neoforged.fml.common.Mod("mdkcosmetic_validation")
public final class ValidationMod {
 public ValidationMod(net.neoforged.bus.api.IEventBus bus){NativeTestFunctions.register(bus);if(net.neoforged.fml.loading.FMLEnvironment.dist.isClient()){
  com.mdk.cosmetic.gametest.legacy.LegacyRegressionInitializer.initialize();
  ClientStartupSmoke.initialize();
 }}
}
