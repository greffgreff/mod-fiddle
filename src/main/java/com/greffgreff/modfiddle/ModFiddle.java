package com.greffgreff.modfiddle;

import com.greffgreff.modfiddle.block.ModBlocks;
import com.greffgreff.modfiddle.item.ModItems;
import com.greffgreff.modfiddle.world.structure.ModStructures;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ModFiddle.MOD_ID)
public class ModFiddle {
    public static final String MOD_ID = "modfiddle";
    public static final Logger LOGGER = LogManager.getLogger();

    public ModFiddle() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);

        ModItems.ITEMS.register(bus);
        ModBlocks.BLOCKS.register(bus);
        ModStructures.STRUCTURES.register(bus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModStructures::setupStructures);
    }
}
