package com.greffgreff.modfiddle.events;

import com.greffgreff.modfiddle.ModFiddle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModFiddle.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DoSomethingOnClick {

    @SubscribeEvent
    public static void doJumpOnClick(PlayerInteractEvent e) {

    }
}
