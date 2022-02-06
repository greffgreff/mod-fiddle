package com.greffgreff.modfiddle.world.structure.structures.bridge.pieces;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.ArrayList;
import java.util.Random;

public class TowerPiece extends AbstractBridgePiece {

    public TowerPiece(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos position, Rotation rotation, Random random, ResourceLocation poolLocation) {
        super(dynamicRegistries, chunkGenerator, templateManager, position, rotation, random, poolLocation);
    }

    @Override
    public AbstractBridgePiece createPiece() {
        JigsawPiece towerSpine = getRandomPillarSpinePiece();
        JigsawPiece towerHead = getRandomPillarHeadPiece();
        AbstractVillagePiece towerSpinePlaced = createAbstractPiece(towerSpine, position, rotation, templateManager);
        AbstractVillagePiece towerHeadPlaced = createAbstractPiece(towerHead, position, rotation, templateManager);
        joinJigsaws(towerSpinePlaced, towerHeadPlaced);
        structurePieces.add(towerSpinePlaced);
        structurePieces.add(towerHeadPlaced);
        return this;
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