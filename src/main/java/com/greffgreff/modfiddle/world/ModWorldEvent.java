package com.greffgreff.modfiddle.world;

import com.greffgreff.modfiddle.ModFiddle;
import com.greffgreff.modfiddle.world.gen.ModStructureGeneration;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModFiddle.MOD_ID)
public class ModWorldEvent {
    @SubscribeEvent
    public static void biomeLoadingEvent(final BiomeLoadingEvent event) {
        ModStructureGeneration.generateStructures(event);
    }

    @SubscribeEvent
    public static void addDimentionalSpacing(WorldEvent.Load event) {
        // handle dimension constrains here
    }
}
