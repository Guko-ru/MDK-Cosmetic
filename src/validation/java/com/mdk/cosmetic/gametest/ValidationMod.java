package com.mdk.cosmetic.gametest;
@net.minecraftforge.fml.common.Mod("mdkcosmetic_validation")
public final class ValidationMod {
 public ValidationMod(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext context){var bus=context.getModBusGroup();NativeTestFunctions.register(bus);if(net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient()){
  com.mdk.cosmetic.gametest.legacy.LegacyRegressionInitializer.initialize();
  ClientStartupSmoke.initialize();
 }}
}
