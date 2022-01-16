package com.greffgreff.modfiddle.core.init;

import com.greffgreff.modfiddle.ModFiddle;
import com.greffgreff.modfiddle._items.MyItem;
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
            () -> new MyItem(new Item.Properties().group(ItemGroup.MISC)));

    public static void registerBlockItem(String blockName, Block block) {
        ItemInit.ITEMS.register(blockName, () -> new BlockItem(block, new Item.Properties().group(ItemGroup.MISC)));
    }
}
