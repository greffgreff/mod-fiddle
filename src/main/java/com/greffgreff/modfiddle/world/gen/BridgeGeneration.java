package com.greffgreff.modfiddle.world.gen;

import com.greffgreff.modfiddle.ModFiddle;
import com.greffgreff.modfiddle.world.structure.structures.bridge.pieces.AbstractBridgePiece;
import com.greffgreff.modfiddle.world.structure.structures.bridge.pieces.DeckPiece;
import com.greffgreff.modfiddle.world.structure.structures.bridge.pieces.TowerPiece;
import com.greffgreff.modfiddle.world.util.Jigsaws;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.List;
import java.util.Random;

public class BridgeGeneration {

    public static void generateBridge(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos startingPos, List<StructurePiece> structurePieces, Random random) {
        ModFiddle.LOGGER.debug("=== STARTING GENERATION ===");

//        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(startingPos.getX() - 80, startingPos.getY() - 80, startingPos.getZ() - 80, startingPos.getX() + 80 + 1, startingPos.getY() + 80 + 1, startingPos.getZ() + 80 + 1);
//        VoxelShape voxel = VoxelShapes.combineAndSimplify(VoxelShapes.create(axisAlignedBB), VoxelShapes.create(AxisAlignedBB.toImmutable(pieceBoundingBox)));
//        VoxelShapes.compare(pieceVoxelShape.getValue(), VoxelShapes.create(AxisAlignedBB.toImmutable(adjustedCandidateBoundingBox).shrink(0.25D)), IBooleanFunction.ONLY_SECOND)

        ResourceLocation loc = new ResourceLocation(ModFiddle.MOD_ID, "bridge/bridge");
        TowerPiece tower = new TowerPiece(dynamicRegistries, chunkGenerator, templateManager, startingPos, Rotation.randomRotation(random), random, loc);
        DeckPiece deck = new DeckPiece(dynamicRegistries, chunkGenerator, templateManager, startingPos, Rotation.NONE, random, loc);

        tower.createPiece();
        deck.createPiece();

        Jigsaws.joinJigsaws(tower, deck, templateManager, random);

        structurePieces.addAll(tower.getPiece());
        structurePieces.addAll(deck.getPiece());

        ModFiddle.LOGGER.debug("=== GENERATION FINISHED ===");
    }
}
