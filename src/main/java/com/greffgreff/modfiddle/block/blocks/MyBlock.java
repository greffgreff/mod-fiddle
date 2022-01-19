package com.greffgreff.modfiddle.block.blocks;


import com.greffgreff.modfiddle.item.items.MyItem;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import java.util.UUID;

public class MyBlock extends Block {

    public MyBlock() {
        super(AbstractBlock.Properties.create(Material.IRON, MaterialColor.BLUE)
                .hardnessAndResistance(15f, 30f)
                .harvestTool(ToolType.PICKAXE).harvestLevel(2)
                .sound(SoundType.METAL));
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        Item item = player.getHeldItem(Hand.MAIN_HAND).getItem();

        if (item instanceof MyItem && !worldIn.isRemote && handIn == Hand.MAIN_HAND) {
            player.sendMessage(new StringTextComponent("hello"), UUID.randomUUID());
        }

        return ActionResultType.PASS;
    }
}
