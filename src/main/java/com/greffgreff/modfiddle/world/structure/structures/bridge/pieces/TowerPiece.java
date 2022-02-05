package com.greffgreff.modfiddle.world.structure.structures.bridge.pieces;

import net.minecraft.block.JigsawBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.List;
import java.util.Random;

public class TowerPiece extends AbstractBridgePiece {

    public TowerPiece(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos position, Rotation rotation, Random random, ResourceLocation poolLocation) {
        super(dynamicRegistries, chunkGenerator, templateManager, position, rotation, random, poolLocation);
    }

    @Override
    public List<StructurePiece> createPiece() {
        JigsawPiece towerSpine = getRandomPillarSpinePiece();
        JigsawPiece towerHead = getRandomPillarHeadPiece();
        AbstractVillagePiece towerSpinePlaced = createAbstractPiece(towerSpine, position, Rotation.NONE); // must work on random rotations
        AbstractVillagePiece towerHeadPlaced = createAbstractPiece(towerHead, position, Rotation.NONE);
        joinJigsaws(towerSpinePlaced, towerHeadPlaced);
        structurePieces.add(towerSpinePlaced);
        structurePieces.add(towerHeadPlaced);
        return structurePieces;

//        MutableBoundingBox towerBB = towerHeadPlaced.getBoundingBox();
//        for (int x = 0; x < towerBB.getXSize(); x++) {
//            for (int z = 0; z < towerBB.getZSize(); z++) {
//                int terrainFloor = chunkGenerator.getNoiseHeight(x + towerSpinePlaced.getPos().getX(), z + towerSpinePlaced.getPos().getZ(), Heightmap.Type.OCEAN_FLOOR_WG);
//                int groundDelta = towerBB.minY - terrainFloor;
//            }
//        }
    }

    private JigsawPiece getRandomPillarSpinePiece() {
        JigsawPiece deckPiece;
        do {
            deckPiece = weightedPieces.next();
        } while (!getPieceName(deckPiece).contains("pillarspine"));
        return deckPiece;
    }

    private JigsawPiece getRandomPillarHeadPiece() {
        JigsawPiece deckPiece;
        do {
            deckPiece = weightedPieces.next();
        } while (!getPieceName(deckPiece).contains("pillarhead"));
        return deckPiece;
    }
}