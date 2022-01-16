package com.greffgreff.modfiddle.core.init;

import com.greffgreff.modfiddle.ModFiddle;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemInit
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModFiddle.MOD_ID);

    public static final RegistryObject<Item> MYITEM = ITEMS.register("my_item",
            () -> new Item(new Item.Properties().group(ItemGroup.MISC)));

    // Block Items
    public static final RegistryObject<Item> MYBLOCK = ITEMS.register("my_block",
            () -> new BlockItem(BlockInit.MYBLOCK.get(), new Item.Properties().group(ItemGroup.MISC)));
}
