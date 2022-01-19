package com.greffgreff.modfiddle.block;

import com.greffgreff.modfiddle.ModFiddle;
import com.greffgreff.modfiddle.block.blocks.MyBlock;
import com.greffgreff.modfiddle.item.ModItems;
import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
    // Setup
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ModFiddle.MOD_ID);

    public static RegistryObject<Block> registerBlock(String blockName, Block block) {
        ModItems.registerBlockItem(blockName, block);
        return BLOCKS.register(blockName, () -> block);
    }

    // Blocks
    public static final RegistryObject<Block> MYBLOCK = registerBlock("my_block", new MyBlock());
}
