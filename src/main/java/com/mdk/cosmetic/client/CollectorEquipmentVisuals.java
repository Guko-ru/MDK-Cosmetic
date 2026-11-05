package com.mdk.cosmetic.client;

import com.mdk.cosmetic.CollectorBackpackData;
import com.mdk.cosmetic.MDKCosmetic;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.item.ItemStack;

/** Visual-only state; the server keeps the real backpack and its contents. */
public final class CollectorEquipmentVisuals {
    private record Info(int model,boolean linked){}
    private static final Map<UUID,Info> MODELS=new HashMap<>();
    private CollectorEquipmentVisuals(){}
    public static void set(String uuid,int model,boolean linked){
        try {MODELS.put(UUID.fromString(uuid),new Info(model,linked));}
        catch (IllegalArgumentException ignored){}
    }
    public static int get(UUID uuid){return MODELS.getOrDefault(uuid,new Info(0,false)).model();}
    public static ItemStack clientBackpack(UUID uuid){
        Info info=MODELS.getOrDefault(uuid,new Info(0,false));
        ItemStack stack=switch(info.model()){
            case 1 -> new ItemStack(MDKCosmetic.COLLECTOR_BACKPACK_BASIC.get());
            case 2 -> new ItemStack(MDKCosmetic.COLLECTOR_BACKPACK_REINFORCED.get());
            default -> ItemStack.EMPTY;
        };
        if(!stack.isEmpty() && info.linked())CollectorBackpackData.flag(stack,"Linked",true);
        return stack;
    }
    public static void clear(){MODELS.clear();}
}
