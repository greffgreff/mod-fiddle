package com.greffgreff.modfiddle.item;

import com.greffgreff.modfiddle.ModFiddle;
import com.greffgreff.modfiddle.item.items.MyItem;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    // Setup
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModFiddle.MOD_ID);

    public static RegistryObject<Item> registerItem(String itemName, Item item) {
        return ModItems.ITEMS.register(itemName, () -> item);
    }

    public static void registerBlockItem(String blockName, Block block) {
        ModItems.ITEMS.register(blockName, () -> new BlockItem(block, new Item.Properties().group(ItemGroup.MISC)));
    }

    // Items
    public static final RegistryObject<Item> MYITEM = registerItem("my_item", new MyItem(new Item.Properties().group(ItemGroup.MISC)));
}
