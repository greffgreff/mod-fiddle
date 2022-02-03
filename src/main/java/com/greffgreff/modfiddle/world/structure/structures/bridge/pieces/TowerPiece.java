package com.greffgreff.modfiddle.world.structure.structures.bridge.pieces;

import com.greffgreff.modfiddle.ModFiddle;
import com.greffgreff.modfiddle.world.structure.structures.bridge.pieces.AbstractBridgePiece;
import com.greffgreff.modfiddle.world.util.WeightedItems;
import net.minecraft.block.JigsawBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.jigsaw.IJigsawDeserializer;
import net.minecraft.world.gen.feature.jigsaw.JigsawJunction;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TowerPiece extends AbstractBridgePiece {

    public TowerPiece(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos startingPosition, Random random, ResourceLocation poolLocation) {
        super(dynamicRegistries, chunkGenerator, templateManager, startingPosition, random, poolLocation);
    }

    @Override
    public List<StructurePiece> createPiece() {
        JigsawPiece towerSpine = getRandomPillarSpinePiece();
        JigsawPiece towerHead = getRandomPillarHeadPiece();
        AbstractVillagePiece towerSpinePlaced = createAbstractPiece(towerSpine, startingPosition, Rotation.randomRotation(this.random));
        AbstractVillagePiece towerHeadPlaced = createAbstractPiece(towerHead, startingPosition, Rotation.randomRotation(this.random));;

        for (Template.BlockInfo towerSpineJigsawBlock : towerSpine.getJigsawBlocks(templateManager, BlockPos.ZERO, Rotation.randomRotation(this.random), random)) {
            for (Template.BlockInfo towerHeadJigsawBlock: towerHead.getJigsawBlocks(templateManager, BlockPos.ZERO, Rotation.randomRotation(this.random), random)) {
                if (JigsawBlock.hasJigsawMatch(towerSpineJigsawBlock, towerHeadJigsawBlock)) {
                    int xDelta = towerSpineJigsawBlock.pos.getX() - towerHeadJigsawBlock.pos.getX();
                    int yDelta = towerSpineJigsawBlock.pos.getY() - towerHeadJigsawBlock.pos.getY();
                    int zDelta = towerSpineJigsawBlock.pos.getZ() - towerHeadJigsawBlock.pos.getZ();
                    towerHeadPlaced.offset(xDelta, yDelta, zDelta);
                }
            }
        }

        this.structurePieces.add(towerSpinePlaced);
        this.structurePieces.add(towerHeadPlaced);

        MutableBoundingBox towerBB = towerHeadPlaced.getBoundingBox();
        for (int x = 0; x < towerBB.getXSize(); x++) {
            for (int z = 0; z < towerBB.getZSize(); z++) {
                int terrainFloor = chunkGenerator.getNoiseHeight(x + towerSpinePlaced.getPos().getX(), z + towerSpinePlaced.getPos().getZ(), Heightmap.Type.OCEAN_FLOOR_WG);
                int groundDelta = towerBB.minY - terrainFloor;
            }
        }

        return this.structurePieces;
    }

    private JigsawPiece getRandomPillarSpinePiece() {
        JigsawPiece deckPiece;
        do {
            deckPiece = this.weightedPieces.next();
        } while (!getPieceName(deckPiece).contains("pillarspine"));
        return deckPiece;
    }

    private JigsawPiece getRandomPillarHeadPiece() {
        JigsawPiece deckPiece;
        do {
            deckPiece = this.weightedPieces.next();
        } while (!getPieceName(deckPiece).contains("pillarhead"));
        return deckPiece;
    }
}