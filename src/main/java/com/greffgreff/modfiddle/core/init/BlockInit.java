package com.greffgreff.modfiddle.core.init;

import com.greffgreff.modfiddle.ModFiddle;
import com.greffgreff.modfiddle._blocks.MyBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockInit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ModFiddle.MOD_ID);

    public static final RegistryObject<Block> MYBLOCK = registerBlock("my_block", new MyBlock());

    public static RegistryObject<Block> registerBlock(String blockName, Block block) {
        ItemInit.registerBlockItem(blockName, block);
        return BLOCKS.register(blockName, () -> block);
    }
}
